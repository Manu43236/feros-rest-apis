package com.feros.api.service.impl;

import com.feros.api.dto.request.AttendanceRequest;
import com.feros.api.dto.request.BulkAttendanceRequest;
import com.feros.api.dto.request.ReviewTripProofRequest;
import com.feros.api.dto.request.TripProofRequest;
import com.feros.api.dto.response.AttendanceResponse;
import com.feros.api.dto.response.TripProofResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.AttendanceType;
import com.feros.api.entity.master.LeaveType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.AttendanceService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        return mapToResponse(saveAttendance(request, tenantId));
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

            responses.add(mapToResponse(saveAttendance(req, tenantId)));
        }

        return responses;
    }

    private Attendance saveAttendance(AttendanceRequest request, Long tenantId) {
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
                .markedAt(LocalDateTime.now())
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
        return attendanceRepository
                .findByTenantIdAndAttendanceDateAndIsActiveTrue(getCurrentTenantId(), date)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<AttendanceResponse> getAttendanceByUser(Long userId, LocalDate from, LocalDate to) {
        return attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrue(
                        userId, getCurrentTenantId(), from, to)
                .stream().map(this::mapToResponse).toList();
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
        attendance.setMarkedAt(LocalDateTime.now());

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
                .capturedAt(request.getCapturedAt() != null ? request.getCapturedAt() : LocalDateTime.now())
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
        proof.setReviewedAt(LocalDateTime.now());
        proof.setReviewRemarks(request.getReviewRemarks());

        return mapToTripProofResponse(tripProofRepository.save(proof));
    }

    // ===================== MAPPERS =====================
    private AttendanceResponse mapToResponse(Attendance a) {
        String roleName = a.getUser().getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse(null);

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
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
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