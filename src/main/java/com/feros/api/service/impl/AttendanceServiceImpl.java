package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.AttendanceRequest;
import com.feros.api.dto.request.BulkAttendanceRequest;
import com.feros.api.dto.request.MarkMobilePresentRequest;
import com.feros.api.dto.request.MarkOwnAttendanceRequest;
import com.feros.api.dto.request.ReviewTripProofRequest;
import com.feros.api.dto.request.TripProofRequest;
import com.feros.api.dto.response.AttendanceResponse;
import com.feros.api.dto.response.TripProofResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.AttendanceType;
import com.feros.api.entity.master.LeaveType;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.AttendanceService;
import com.feros.api.service.NotificationService;
import com.feros.api.service.S3Service;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TripProofRepository tripProofRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final LrRepository lrRepository;
    private final AttendanceTypeRepository attendanceTypeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final VehicleRepository vehicleRepository;
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Long tenantId = getCurrentTenantId();

        if (attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                request.getUserId(), tenantId, request.getAttendanceDate())) {
            throw new FerosException("Attendance already marked for this user on " +
                    request.getAttendanceDate(), HttpStatus.CONFLICT);
        }

        return mapToResponse(saveAttendance(request, tenantId, AttendanceApprovalStatus.APPROVED));
    }

    @Override
    @Transactional
    public AttendanceResponse markOwnAttendance(MarkOwnAttendanceRequest request) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDate today = TimeUtil.today();

        if (attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                currentUserId, tenantId, today)) {
            throw new FerosException("You have already marked attendance for today", HttpStatus.CONFLICT);
        }

        AttendanceRequest req = new AttendanceRequest();
        req.setUserId(currentUserId);
        req.setAttendanceDate(today);
        req.setAttendanceTypeId(request.getAttendanceTypeId());
        req.setLeaveTypeId(request.getLeaveTypeId());
        req.setLeaveReason(request.getLeaveReason());
        req.setRemarks(request.getRemarks());

        Attendance saved = saveAttendance(req, tenantId, AttendanceApprovalStatus.PENDING);
        if (request.getSelfieUrl() != null) saved.setSelfieUrl(request.getSelfieUrl());
        if (request.getLatitude()  != null) saved.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) saved.setLongitude(request.getLongitude());
        Attendance finalSaved = attendanceRepository.save(saved);
        notificationService.sendToRoles(finalSaved.getTenant(), List.of(RoleName.SUPERVISOR),
                NotificationType.ATTENDANCE_PENDING,
                "Attendance Pending Approval",
                finalSaved.getUser().getName() + " has marked attendance for " + finalSaved.getAttendanceDate() + ". Please review.");
        return mapToResponse(finalSaved);
    }

    @Override
    @Transactional
    public AttendanceResponse approveAttendance(Long id) {
        Attendance attendance = attendanceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Attendance not found", HttpStatus.NOT_FOUND));

        if (attendance.getApprovalStatus() != AttendanceApprovalStatus.PENDING) {
            throw new FerosException("Only PENDING attendance can be approved", HttpStatus.BAD_REQUEST);
        }

        attendance.setApprovalStatus(AttendanceApprovalStatus.APPROVED);
        attendance.setApprovedBy(getCurrentUser());
        attendance.setApprovedAt(TimeUtil.nowIst());
        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse rejectAttendance(Long id) {
        Attendance attendance = attendanceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Attendance not found", HttpStatus.NOT_FOUND));

        if (attendance.getApprovalStatus() != AttendanceApprovalStatus.PENDING) {
            throw new FerosException("Only PENDING attendance can be rejected", HttpStatus.BAD_REQUEST);
        }

        attendance.setApprovalStatus(AttendanceApprovalStatus.REJECTED);
        attendance.setApprovedBy(getCurrentUser());
        attendance.setApprovedAt(TimeUtil.nowIst());
        Attendance saved = attendanceRepository.save(attendance);
        notificationService.sendToUser(saved.getTenant(), saved.getUser(), NotificationType.ATTENDANCE_REJECTED,
                "Attendance Rejected",
                "Your attendance for " + saved.getAttendanceDate() + " has been rejected. Please re-submit.");
        return mapToResponse(saved);
    }

    @Override
    public List<AttendanceResponse> getPendingAttendance() {
        return attendanceRepository
                .findByTenantIdAndApprovalStatusAndIsActiveTrue(getCurrentTenantId(), AttendanceApprovalStatus.PENDING)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<AttendanceResponse> getRejectedAttendance() {
        return attendanceRepository
                .findByTenantIdAndApprovalStatusAndIsActiveTrue(getCurrentTenantId(), AttendanceApprovalStatus.REJECTED)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<AttendanceResponse> getMyAttendance(java.time.LocalDate from, java.time.LocalDate to) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrueOrderByAttendanceDateDesc(
                        currentUserId, getCurrentTenantId(), from, to)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request) {
        Long tenantId = getCurrentTenantId();
        List<AttendanceResponse> responses = new ArrayList<>();

        for (BulkAttendanceRequest.AttendanceEntry entry : request.getEntries()) {
            if (attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                    entry.getUserId(), tenantId, request.getAttendanceDate())) {
                continue; // skip already marked, don't throw
            }

            AttendanceRequest req = new AttendanceRequest();
            req.setUserId(entry.getUserId());
            req.setAttendanceDate(request.getAttendanceDate());
            req.setAttendanceTypeId(entry.getAttendanceTypeId());
            req.setLeaveTypeId(entry.getLeaveTypeId());
            req.setLeaveReason(entry.getLeaveReason());
            req.setRemarks(entry.getRemarks());

            responses.add(mapToResponse(saveAttendance(req, tenantId, AttendanceApprovalStatus.APPROVED)));
        }

        return responses;
    }

    private Attendance saveAttendance(AttendanceRequest request, Long tenantId, AttendanceApprovalStatus approvalStatus) {
        Tenant tenant = getCurrentTenant();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        AttendanceType attendanceType = attendanceTypeRepository
                .findById(request.getAttendanceTypeId())
                .orElseThrow(() -> new FerosException("Attendance type not found",
                        HttpStatus.NOT_FOUND));

        Attendance attendance = Attendance.builder()
                .tenant(tenant)
                .user(user)
                .attendanceDate(request.getAttendanceDate())
                .attendanceType(attendanceType)
                .leaveReason(request.getLeaveReason())
                .remarks(request.getRemarks())
                .markedBy(getCurrentUser())
                .markedAt(TimeUtil.nowIst())
                .approvalStatus(approvalStatus)
                .isActive(true)
                .build();

        if (request.getLeaveTypeId() != null) {
            LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                    .orElseThrow(() -> new FerosException("Leave type not found",
                            HttpStatus.NOT_FOUND));
            attendance.setLeaveType(leaveType);
        }

        return attendanceRepository.save(attendance);
    }

    @Override
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        String role = SecurityUtil.getCurrentRole();
        return attendanceRepository
                .findByTenantIdAndAttendanceDateAndIsActiveTrue(getCurrentTenantId(), date)
                .stream()
                .filter(a -> isVisibleToRole(a, role))
                .map(this::mapToResponse).toList();
    }

    private boolean isVisibleToRole(Attendance a, String callerRole) {
        String targetRole = a.getUser().getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse("");
        return switch (callerRole) {
            case "SUPERVISOR" -> targetRole.equals("DRIVER") || targetRole.equals("CLEANER");
            case "OFFICE_STAFF" -> !targetRole.equals("ADMIN");
            default -> true; // ADMIN, SUPER_ADMIN see all
        };
    }

    @Override
    public List<AttendanceResponse> getAttendanceByUser(Long userId, LocalDate from, LocalDate to) {
        String role = SecurityUtil.getCurrentRole();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
        String targetRole = targetUser.getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse("");

        if ("SUPERVISOR".equals(role) && !targetRole.equals("DRIVER") && !targetRole.equals("CLEANER")) {
            throw new FerosException("Access denied", HttpStatus.FORBIDDEN);
        }
        if ("OFFICE_STAFF".equals(role) && targetRole.equals("ADMIN")) {
            throw new FerosException("Access denied", HttpStatus.FORBIDDEN);
        }

        return attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrue(
                        userId, getCurrentTenantId(), from, to)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public List<AttendanceResponse> bulkApproveAttendance(List<Long> ids) {
        Long tenantId = getCurrentTenantId();
        User approver = getCurrentUser();
        List<AttendanceResponse> results = new ArrayList<>();
        for (Long id : ids) {
            Attendance attendance = attendanceRepository
                    .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                    .orElseThrow(() -> new FerosException("Attendance not found: " + id, HttpStatus.NOT_FOUND));
            if (attendance.getApprovalStatus() == AttendanceApprovalStatus.PENDING) {
                attendance.setApprovalStatus(AttendanceApprovalStatus.APPROVED);
                attendance.setApprovedBy(approver);
                attendance.setApprovedAt(TimeUtil.nowIst());
                results.add(mapToResponse(attendanceRepository.save(attendance)));
            }
        }
        return results;
    }

    @Override
    @Transactional
    public List<AttendanceResponse> bulkRejectAttendance(List<Long> ids) {
        Long tenantId = getCurrentTenantId();
        User approver = getCurrentUser();
        List<AttendanceResponse> results = new ArrayList<>();
        for (Long id : ids) {
            Attendance attendance = attendanceRepository
                    .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                    .orElseThrow(() -> new FerosException("Attendance not found: " + id, HttpStatus.NOT_FOUND));
            if (attendance.getApprovalStatus() == AttendanceApprovalStatus.PENDING) {
                attendance.setApprovalStatus(AttendanceApprovalStatus.REJECTED);
                attendance.setApprovedBy(approver);
                attendance.setApprovedAt(TimeUtil.nowIst());
                Attendance saved = attendanceRepository.save(attendance);
                notificationService.sendToUser(saved.getTenant(), saved.getUser(), NotificationType.ATTENDANCE_REJECTED,
                        "Attendance Rejected",
                        "Your attendance for " + saved.getAttendanceDate() + " has been rejected. Please re-submit.");
                results.add(mapToResponse(saved));
            }
        }
        return results;
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Attendance not found", HttpStatus.NOT_FOUND));

        AttendanceType attendanceType = attendanceTypeRepository
                .findById(request.getAttendanceTypeId())
                .orElseThrow(() -> new FerosException("Attendance type not found",
                        HttpStatus.NOT_FOUND));

        attendance.setAttendanceType(attendanceType);
        attendance.setLeaveReason(request.getLeaveReason());
        attendance.setRemarks(request.getRemarks());
        attendance.setMarkedBy(getCurrentUser());
        attendance.setMarkedAt(TimeUtil.nowIst());

        if (request.getLeaveTypeId() != null) {
            LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                    .orElseThrow(() -> new FerosException("Leave type not found",
                            HttpStatus.NOT_FOUND));
            attendance.setLeaveType(leaveType);
        } else {
            attendance.setLeaveType(null);
        }

        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public TripProofResponse addTripProof(Long userId, TripProofRequest request) {
        Long tenantId = getCurrentTenantId();

        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(request.getLrId(), tenantId)
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        TripProof proof = TripProof.builder()
                .tenant(getCurrentTenant())
                .lr(lr)
                .user(user)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(request.getImageUrl())
                .capturedAt(request.getCapturedAt() != null ? request.getCapturedAt() : TimeUtil.nowIst())
                .isReviewed(false)
                .isActive(true)
                .build();

        return mapToTripProofResponse(tripProofRepository.save(proof));
    }

    @Override
    public List<TripProofResponse> getTripProofsByLr(Long lrId) {
        return tripProofRepository.findByLrIdAndIsActiveTrue(lrId)
                .stream().map(this::mapToTripProofResponse).toList();
    }

    @Override
    public List<TripProofResponse> getTripProofsByUser(Long userId) {
        return tripProofRepository.findByUserIdAndIsActiveTrue(userId)
                .stream().map(this::mapToTripProofResponse).toList();
    }

    @Override
    @Transactional
    public TripProofResponse reviewTripProof(Long proofId, ReviewTripProofRequest request) {
        TripProof proof = tripProofRepository
                .findByIdAndTenantIdAndIsActiveTrue(proofId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Trip proof not found", HttpStatus.NOT_FOUND));

        proof.setIsReviewed(true);
        proof.setReviewedBy(getCurrentUser());
        proof.setReviewedAt(TimeUtil.nowIst());
        proof.setReviewRemarks(request.getReviewRemarks());

        return mapToTripProofResponse(tripProofRepository.save(proof));
    }

    // ===================== MAPPERS =====================
    private AttendanceResponse mapToResponse(Attendance a) {
        String roleName = a.getUser().getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse(null);

        // Duty hours & label
        String dutyLabel = "Full Day";
        Double dutyHours = null;
        boolean canUndoOut = false;

        if (a.getMarkedOutAt() != null) {
            long totalMinutes = Duration.between(a.getMarkedAt(), a.getMarkedOutAt()).toMinutes();
            dutyHours  = Math.round(totalMinutes / 6.0) / 10.0; // 1 decimal place
            dutyLabel  = dutyHours + " hrs";
            canUndoOut = Duration.between(a.getMarkedOutAt(), TimeUtil.nowIst()).toMinutes() <= 10;
        }

        return AttendanceResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .userName(a.getUser().getName())
                .userPhone(a.getUser().getPhone())
                .roleName(roleName)
                .attendanceDate(a.getAttendanceDate())
                .attendanceTypeId(a.getAttendanceType().getId())
                .attendanceTypeName(a.getAttendanceType().getName())
                .leaveTypeId(a.getLeaveType() != null ? a.getLeaveType().getId() : null)
                .leaveTypeName(a.getLeaveType() != null ? a.getLeaveType().getName() : null)
                .leaveReason(a.getLeaveReason())
                .markedById(a.getMarkedBy().getId())
                .markedByName(a.getMarkedBy().getName())
                .markedAt(a.getMarkedAt())
                .markedOutAt(a.getMarkedOutAt())
                .dutyLabel(dutyLabel)
                .dutyHours(dutyHours)
                .canUndoOut(canUndoOut)
                .remarks(a.getRemarks())
                .approvalStatus(a.getApprovalStatus())
                .approvedById(a.getApprovedBy() != null ? a.getApprovedBy().getId() : null)
                .approvedByName(a.getApprovedBy() != null ? a.getApprovedBy().getName() : null)
                .approvedAt(a.getApprovedAt())
                .selfieUrl(a.getSelfieUrl() != null ? s3Service.getPublicUrl(a.getSelfieUrl()) : null)
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AttendanceResponse markMobilePresent(MarkMobilePresentRequest request) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDate today = TimeUtil.today();

        if (attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrueAndApprovalStatusNot(
                currentUserId, tenantId, today, AttendanceApprovalStatus.REJECTED)) {
            throw new FerosException("You have already marked attendance for today", HttpStatus.CONFLICT);
        }

        com.feros.api.entity.master.AttendanceType presentType = attendanceTypeRepository
                .findByNameIgnoreCase("PRESENT")
                .orElseThrow(() -> new FerosException(
                        "PRESENT attendance type not configured. Contact admin.", HttpStatus.NOT_FOUND));

        User user = getCurrentUser();

        // If a rejected record exists for today, reset it instead of inserting a new one
        // (unique constraint allows only one record per user per day)
        Attendance attendance = attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(currentUserId, tenantId, today)
                .orElseGet(() -> Attendance.builder()
                        .tenant(getCurrentTenant())
                        .user(user)
                        .attendanceDate(today)
                        .isActive(true)
                        .build());

        attendance.setAttendanceType(presentType);
        attendance.setSelfieUrl(request.getSelfieUrl());
        attendance.setLatitude(request.getLatitude());
        attendance.setLongitude(request.getLongitude());
        attendance.setRemarks(request.getRemarks());
        attendance.setMarkedBy(user);
        attendance.setMarkedAt(TimeUtil.nowIst());
        attendance.setApprovalStatus(AttendanceApprovalStatus.PENDING);
        attendance.setApprovedBy(null);
        attendance.setApprovedAt(null);

        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse getTodayAttendanceStatus() {
        return attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                        SecurityUtil.getCurrentUserId(), getCurrentTenantId(), TimeUtil.today())
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public AttendanceResponse markOut() {
        Long userId   = SecurityUtil.getCurrentUserId();
        Long tenantId = getCurrentTenantId();

        Attendance attendance = attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(userId, tenantId, TimeUtil.today())
                .orElseThrow(() -> new FerosException("No attendance record found for today", HttpStatus.NOT_FOUND));

        if (attendance.getApprovalStatus() == AttendanceApprovalStatus.REJECTED) {
            throw new FerosException("Cannot mark out — attendance is rejected", HttpStatus.BAD_REQUEST);
        }
        if (attendance.getMarkedOutAt() != null) {
            throw new FerosException("Already marked out for today", HttpStatus.CONFLICT);
        }

        attendance.setMarkedOutAt(TimeUtil.nowIst());
        AttendanceResponse response = mapToResponse(attendanceRepository.save(attendance));

        // Auto-unassign from vehicle on mark-out
        vehicleRepository.findAssignedVehicleByStaffUserId(userId).ifPresent(vehicle -> {
            vehicleStaffAssignmentRepository
                    .findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(userId, tenantId)
                    .ifPresent(a -> {
                        a.setAssignedTo(TimeUtil.today());
                        vehicleStaffAssignmentRepository.save(a);
                    });
            if (vehicle.getCurrentDriver() != null && vehicle.getCurrentDriver().getId().equals(userId)) {
                vehicle.setCurrentDriver(null);
            } else if (vehicle.getCurrentCleaner() != null && vehicle.getCurrentCleaner().getId().equals(userId)) {
                vehicle.setCurrentCleaner(null);
            }
            vehicleRepository.save(vehicle);
        });

        return response;
    }

    @Override
    @Transactional
    public AttendanceResponse undoOut() {
        Long userId   = SecurityUtil.getCurrentUserId();
        Long tenantId = getCurrentTenantId();

        Attendance attendance = attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(userId, tenantId, TimeUtil.today())
                .orElseThrow(() -> new FerosException("No attendance record found for today", HttpStatus.NOT_FOUND));

        if (attendance.getMarkedOutAt() == null) {
            throw new FerosException("No out record to undo", HttpStatus.BAD_REQUEST);
        }

        long minutesSinceOut = Duration.between(attendance.getMarkedOutAt(), TimeUtil.nowIst()).toMinutes();
        if (minutesSinceOut > 10) {
            throw new FerosException("Undo window expired — out was marked more than 10 minutes ago", HttpStatus.BAD_REQUEST);
        }

        attendance.setMarkedOutAt(null);
        return mapToResponse(attendanceRepository.save(attendance));
    }

    private TripProofResponse mapToTripProofResponse(TripProof p) {
        return TripProofResponse.builder()
                .id(p.getId())
                .lrId(p.getLr().getId())
                .lrNumber(p.getLr().getLrNumber())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .imageUrl(p.getImageUrl())
                .capturedAt(p.getCapturedAt())
                .isReviewed(p.getIsReviewed())
                .reviewedById(p.getReviewedBy() != null ? p.getReviewedBy().getId() : null)
                .reviewedByName(p.getReviewedBy() != null ? p.getReviewedBy().getName() : null)
                .reviewedAt(p.getReviewedAt())
                .reviewRemarks(p.getReviewRemarks())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}