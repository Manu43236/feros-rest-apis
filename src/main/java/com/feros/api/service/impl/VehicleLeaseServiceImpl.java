package com.feros.api.service.impl;

import com.feros.api.dto.request.AssignCleanerRequest;
import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.AssignDriverRequest;
import com.feros.api.dto.request.LeaseSessionStartRequest;
import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
import com.feros.api.dto.request.ShiftDefinitionRequest;
import com.feros.api.dto.request.VehicleLeaseRequest;
import com.feros.api.dto.response.LeaseBillingResponse;
import com.feros.api.dto.response.LeaseDailyLogResponse;
import com.feros.api.dto.response.LeaseVehicleAssignmentResponse;
import com.feros.api.dto.response.LeaseVehicleSessionResponse;
import com.feros.api.dto.response.VehicleLeaseResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.LeaseStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RateType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.service.NotificationService;
import com.feros.api.repository.LeaseDailyLogRepository;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.VehicleLeaseService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.util.TimeUtil;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleLeaseServiceImpl implements VehicleLeaseService {

    private final VehicleLeaseRepository leaseRepository;
    private final LeaseVehicleAssignmentRepository assignmentRepository;
    private final LeaseVehicleSessionRepository sessionRepository;
    private final LeaseDailyLogRepository dailyLogRepository;
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final ClientDivisionRepository clientDivisionRepository;
    private final NumberGeneratorService numberGenerator;
    private final NotificationService notificationService;
    private final LeaseDriverAssignmentLogRepository leaseDriverLogRepository;
    private final LeaseCleanerAssignmentLogRepository leaseCleanerLogRepository;
    private final com.feros.api.repository.LeaseShiftRepository leaseShiftRepository;
    private final com.feros.api.repository.LeaseShiftAssignmentRepository leaseShiftAssignmentRepository;
    private final com.feros.api.repository.AttendanceTypeRepository attendanceTypeRepository;
    private final OrderVehicleAllocationRepository orderVehicleAllocationRepository;
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
    private final UserRepository userRepository;
    private final com.feros.api.repository.AttendanceRepository attendanceRepository;
    private final com.feros.api.service.StaffAssignmentGuard staffAssignmentGuard;

    private Long tenantId() { return SecurityUtil.getCurrentTenantId(); }

    private Tenant tenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private VehicleLease fetchLease(Long id) {
        return leaseRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId())
                .orElseThrow(() -> new FerosException("Lease not found", HttpStatus.NOT_FOUND));
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Override
    public Page<VehicleLeaseResponse> getAll(int page, int size, LeaseStatus status, Long clientId, String search) {
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        return leaseRepository.findAllPaged(tenantId(), status, clientId, q, PageRequest.of(page, size))
                .map(lease -> toResponse(lease, assignmentRepository.countByLeaseId(lease.getId())));
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    @Override
    public VehicleLeaseResponse getById(Long id) {
        VehicleLease lease = fetchLease(id);
        return toResponse(lease, assignmentRepository.countByLeaseId(id));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VehicleLeaseResponse create(VehicleLeaseRequest request) {
        Tenant t = tenant();
        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        VehicleLease lease = VehicleLease.builder()
                .tenant(t)
                .leaseNumber(numberGenerator.generateFY(t.getId(), NumberUtil.Type.LSE))
                .client(client)
                .site(request.getSite())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rateType(request.getRateType())
                .notes(request.getNotes())
                .shiftDuty(Boolean.TRUE.equals(request.getShiftDuty()))
                .build();

        VehicleLease saved = leaseRepository.save(lease);
        persistShifts(saved, request);
        notificationService.sendToRoles(t, List.of(RoleName.ADMIN, RoleName.OFFICE_STAFF, RoleName.SUPERVISOR),
                NotificationType.LEASE_CREATED,
                "New Vehicle Lease",
                "Lease " + saved.getLeaseNumber() + " for " + client.getClientName() + " has been created.",
                Map.of("type", "LEASE_CREATED", "leaseId", String.valueOf(saved.getId())));
        return toResponse(saved, 0);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VehicleLeaseResponse update(Long id, VehicleLeaseRequest request) {
        VehicleLease lease = fetchLease(id);
        if (lease.getStatus() == LeaseStatus.CLOSED)
            throw new FerosException("Cannot edit a closed lease", HttpStatus.BAD_REQUEST);

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        lease.setClient(client);
        lease.setSite(request.getSite());
        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        lease.setRateType(request.getRateType());
        lease.setNotes(request.getNotes());
        if (request.getShiftDuty() != null) lease.setShiftDuty(request.getShiftDuty());

        VehicleLease saved = leaseRepository.save(lease);
        // Re-defining shifts replaces the set (forward-only pay lives on the assignment snapshots,
        // so already-worked days are unaffected by redefining the lease's shift windows).
        if (request.getShifts() != null) persistShifts(saved, request);

        return toResponse(saved, assignmentRepository.countByLeaseId(id));
    }

    // ── Status ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VehicleLeaseResponse updateStatus(Long id, LeaseStatus newStatus) {
        VehicleLease lease = fetchLease(id);

        validateStatusTransition(lease.getStatus(), newStatus);

        if (newStatus == LeaseStatus.CLOSED) {
            if (sessionRepository.existsByAssignment_Lease_IdAndIsActiveTrue(id))
                throw new FerosException(
                        "Cannot close lease — one or more vehicles have active sessions in progress. End all sessions first.",
                        HttpStatus.CONFLICT);

            // Auto-close open driver logs, assignments, and revert vehicle status
            List<LeaseVehicleAssignment> active = assignmentRepository
                    .findByLeaseIdOrderByStartDateAsc(id)
                    .stream().filter(a -> Boolean.TRUE.equals(a.getIsActive())).collect(Collectors.toList());
            LocalDateTime now = LocalDateTime.now();

            // Release every open driver log for the lease — including ones orphaned on
            // already-inactive assignments, which the active-only loop below would miss
            List<LeaseDriverAssignmentLog> openLogs = leaseDriverLogRepository.findOpenByLeaseId(id);
            openLogs.forEach(log -> log.setUnassignedAt(now));
            leaseDriverLogRepository.saveAll(openLogs);

            List<LeaseCleanerAssignmentLog> openCleanerLogs = leaseCleanerLogRepository.findOpenByLeaseId(id);
            openCleanerLogs.forEach(log -> log.setUnassignedAt(now));
            leaseCleanerLogRepository.saveAll(openCleanerLogs);

            // Shift-duty leases: drop shift assignments from the close date forward so a closed lease
            // can never pin a driver/cleaner or block their reassignment. Past days are kept for payroll.
            List<LeaseShiftAssignment> futureShifts = leaseShiftAssignmentRepository
                    .findByLeaseIdAndWorkDateBetween(id, now.toLocalDate(), now.toLocalDate().plusYears(5));
            leaseShiftAssignmentRepository.deleteAll(futureShifts);

            active.forEach(a -> {
                a.setIsActive(false);
                a.setEndDate(LocalDate.now());
                revertVehicleStatus(a.getVehicle());
            });
            assignmentRepository.saveAll(active);
        }

        lease.setStatus(newStatus);

        return toResponse(leaseRepository.save(lease), assignmentRepository.countByLeaseId(id));
    }

    private void validateStatusTransition(LeaseStatus current, LeaseStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == LeaseStatus.ACTIVE;
            case ACTIVE -> next == LeaseStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!valid) throw new FerosException(
                "Cannot transition from " + current + " to " + next, HttpStatus.BAD_REQUEST);
    }

    // ── Extend ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VehicleLeaseResponse extend(Long id, LocalDate newEndDate) {
        VehicleLease lease = fetchLease(id);
        if (lease.getStatus() == LeaseStatus.CLOSED)
            throw new FerosException("Cannot extend a closed lease", HttpStatus.BAD_REQUEST);
        if (newEndDate.isBefore(lease.getStartDate()))
            throw new FerosException("End date must be after start date", HttpStatus.BAD_REQUEST);

        lease.setEndDate(newEndDate);
        return toResponse(leaseRepository.save(lease), assignmentRepository.countByLeaseId(id));
    }

    // ── Vehicles ──────────────────────────────────────────────────────────────

    @Override
    public List<LeaseVehicleAssignmentResponse> getVehicles(Long leaseId) {
        fetchLease(leaseId);
        return assignmentRepository.findByLeaseIdOrderByStartDateAsc(leaseId)
                .stream().map(this::toAssignmentResponse).collect(Collectors.toList());
    }

    // ── Shift assignment (shift-duty leases) ────────────────────────────────────

    @Override
    @Transactional
    public com.feros.api.dto.response.LeaseShiftAssignmentResponse assignShift(
            Long leaseId, Long assignmentId, com.feros.api.dto.request.ShiftAssignmentRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));
        com.feros.api.entity.LeaseShift shift = leaseShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new FerosException("Shift not found", HttpStatus.NOT_FOUND));
        if (!shift.getLease().getId().equals(leaseId))
            throw new FerosException("Shift does not belong to this lease", HttpStatus.BAD_REQUEST);

        StaffProfile driver = resolveShiftStaff(request.getDriverStaffId(), shift, request.getWorkDate(), false);
        StaffProfile cleaner = resolveShiftStaff(request.getCleanerStaffId(), shift, request.getWorkDate(), true);

        // Upsert on (assignment, shift, workDate)
        LeaseShiftAssignment row = leaseShiftAssignmentRepository
                .findByLeaseVehicleAssignmentIdAndLeaseShiftIdAndWorkDate(assignmentId, shift.getId(), request.getWorkDate())
                .orElseGet(() -> LeaseShiftAssignment.builder()
                        .leaseVehicleAssignment(assignment)
                        .leaseShift(shift)
                        .workDate(request.getWorkDate())
                        .tenant(tenant())
                        .build());
        StaffProfile oldDriver = row.getDriverStaff();
        StaffProfile oldCleaner = row.getCleanerStaff();

        // One-vehicle rule (unassign-first) — only for a NEWLY-assigned person (not a no-op re-save):
        // a shift driver/cleaner must not be open on a DIFFERENT vehicle via a standing lease, VSA,
        // order, or in-progress work. Shift↔shift is governed by resolveShiftStaff's time-overlap check.
        if (isChangedStaff(oldDriver, driver)) assertShiftStaffFree(driver, assignment.getVehicle().getId());
        if (isChangedStaff(oldCleaner, cleaner)) assertShiftStaffFree(cleaner, assignment.getVehicle().getId());

        row.setDriverStaff(driver);
        row.setCleanerStaff(cleaner);
        // Snapshot the shift pay at assignment time → later pay edits are forward-only
        row.setDriverPaySnapshot(driver != null ? shift.getDriverPay() : null);
        row.setCleanerPaySnapshot(cleaner != null ? shift.getCleanerPay() : null);
        row.setAssignedAt(LocalDateTime.now());
        row.setAssignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null));
        LeaseShiftAssignment saved = leaseShiftAssignmentRepository.save(row);

        // Decision A: anchor attendance to the shift's start date so attendance == payroll date.
        ensureAttendance(driver, request.getWorkDate());
        ensureAttendance(cleaner, request.getWorkDate());
        // Item 4: a replaced crew member with no remaining shift that day loses their auto-marked,
        // still-unapproved attendance (approved records are never touched).
        clearStrayShiftAttendance(oldDriver, driver, request.getWorkDate());
        clearStrayShiftAttendance(oldCleaner, cleaner, request.getWorkDate());

        notifyShift(assignment, shift, driver, request.getWorkDate());
        notifyShift(assignment, shift, cleaner, request.getWorkDate());
        return toShiftAssignmentResponse(saved);
    }

    @Override
    public List<com.feros.api.dto.response.LeaseShiftAssignmentResponse> getShiftAssignments(
            Long leaseId, LocalDate from, LocalDate to) {
        fetchLease(leaseId);
        return leaseShiftAssignmentRepository.findByLeaseIdAndWorkDateBetween(leaseId, from, to)
                .stream().map(this::toShiftAssignmentResponse).collect(Collectors.toList());
    }

    /** Enforce the one-vehicle rule for a shift crew member: not in-progress elsewhere, and not open on
     *  a DIFFERENT vehicle via a standing lease log / VSA / active order. Null = client's own (no-op). */
    private void assertShiftStaffFree(StaffProfile sp, Long vehicleId) {
        if (sp == null) return;
        Long userId = sp.getUser().getId();
        String name = sp.getUser().getName();
        staffAssignmentGuard.assertNotInProgress(userId, tenantId(), name);
        java.util.function.Consumer<LeaseVehicleAssignment> block = va -> {
            throw new FerosException(name + " is already assigned to "
                    + va.getVehicle().getRegistrationNumber() + " (lease " + va.getLease().getLeaseNumber()
                    + "). Unassign them first.", HttpStatus.CONFLICT);
        };
        leaseDriverLogRepository.findActiveByDriverStaffId(sp.getId(), tenantId())
                .map(LeaseDriverAssignmentLog::getLeaseVehicleAssignment)
                .filter(va -> !va.getVehicle().getId().equals(vehicleId))
                .ifPresent(block);
        leaseCleanerLogRepository.findActiveByCleanerStaffId(sp.getId(), tenantId())
                .map(LeaseCleanerAssignmentLog::getLeaseVehicleAssignment)
                .filter(va -> !va.getVehicle().getId().equals(vehicleId))
                .ifPresent(block);
        staffAssignmentGuard.assertNotOnNormalOrOrder(userId, tenantId(), vehicleId, name);
    }

    /** Resolve a staff id to a profile (null = client's own), and reject a time-overlapping shift the
     *  same day. isCleaner picks which side of existing rows to check. */
    private StaffProfile resolveShiftStaff(Long staffUserId, com.feros.api.entity.LeaseShift shift,
                                           LocalDate workDate, boolean isCleaner) {
        if (staffUserId == null) return null; // client's own
        StaffProfile sp = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(staffUserId, tenantId())
                .orElseThrow(() -> new FerosException((isCleaner ? "Cleaner" : "Driver") + " not found", HttpStatus.NOT_FOUND));

        List<LeaseShiftAssignment> sameDay = isCleaner
                ? leaseShiftAssignmentRepository.findByCleanerStaffIdAndWorkDate(sp.getId(), workDate)
                : leaseShiftAssignmentRepository.findByDriverStaffIdAndWorkDate(sp.getId(), workDate);
        for (LeaseShiftAssignment other : sameDay) {
            if (other.getLeaseShift().getId().equals(shift.getId())) continue; // same slot = re-assign
            if (windowsOverlap(shift, other.getLeaseShift()))
                throw new FerosException(
                        sp.getUser().getName() + " is already on an overlapping shift on " + workDate + ".",
                        HttpStatus.CONFLICT);
        }
        return sp;
    }

    private boolean windowsOverlap(com.feros.api.entity.LeaseShift a, com.feros.api.entity.LeaseShift b) {
        return intervalsOverlap(toIntervals(a), toIntervals(b));
    }

    /** True if the shift's window covers the current (IST) time — night shift crosses midnight. */
    private boolean shiftCoversNow(com.feros.api.entity.LeaseShift s) {
        java.time.LocalTime now = TimeUtil.nowIst().toLocalTime();
        java.time.LocalTime st = s.getStartTime(), en = s.getEndTime();
        return st.isBefore(en)
                ? (!now.isBefore(st) && now.isBefore(en))
                : (!now.isBefore(st) || now.isBefore(en));
    }

    private static List<int[]> toIntervals(com.feros.api.entity.LeaseShift s) {
        int from = s.getStartTime().toSecondOfDay() / 60;
        int to = s.getEndTime().toSecondOfDay() / 60;
        List<int[]> out = new java.util.ArrayList<>();
        if (from < to) out.add(new int[]{from, to});
        else { out.add(new int[]{from, 1440}); out.add(new int[]{0, to}); }
        return out;
    }

    private static boolean intervalsOverlap(List<int[]> xs, List<int[]> ys) {
        for (int[] a : xs) for (int[] b : ys) if (a[0] < b[1] && b[0] < a[1]) return true;
        return false;
    }

    /** Decision A: ensure a present attendance exists on workDate (PENDING if newly created).
     *  Never downgrades an existing record; approval still gates payroll. No-op for client staff. */
    private void ensureAttendance(StaffProfile staff, LocalDate workDate) {
        if (staff == null) return;
        Long userId = staff.getUser().getId();
        if (attendanceRepository.findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(userId, tenantId(), workDate).isPresent())
            return;
        com.feros.api.entity.master.AttendanceType present = attendanceTypeRepository.findByNameIgnoreCase("PRESENT")
                .orElseThrow(() -> new FerosException("PRESENT attendance type not configured.", HttpStatus.NOT_FOUND));
        User currentUser = userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null);
        attendanceRepository.save(com.feros.api.entity.Attendance.builder()
                .tenant(tenant())
                .user(staff.getUser())
                .attendanceDate(workDate)
                .attendanceType(present)
                .markedBy(currentUser)
                .markedAt(LocalDateTime.now())
                .approvalStatus(com.feros.api.enums.AttendanceApprovalStatus.PENDING)
                .isActive(true)
                .build());
    }

    private static boolean isChangedStaff(StaffProfile old, StaffProfile neu) {
        return neu != null && (old == null || !old.getId().equals(neu.getId()));
    }

    /** When a shift's crew member is replaced and has no remaining shift that day, retire the present
     *  record the shift auto-created for them — but only if still unapproved. Approved attendance is
     *  payroll truth and is never touched. Soft delete (is_active=false) so it's reversible. */
    private void clearStrayShiftAttendance(StaffProfile old, StaffProfile neu, LocalDate date) {
        if (old == null || (neu != null && old.getId().equals(neu.getId()))) return;
        boolean stillOnShift =
                !leaseShiftAssignmentRepository.findByDriverStaffIdAndWorkDate(old.getId(), date).isEmpty()
             || !leaseShiftAssignmentRepository.findByCleanerStaffIdAndWorkDate(old.getId(), date).isEmpty();
        if (stillOnShift) return;
        attendanceRepository.findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                        old.getUser().getId(), tenantId(), date)
                .filter(a -> a.getApprovalStatus() == com.feros.api.enums.AttendanceApprovalStatus.PENDING)
                .ifPresent(a -> { a.setIsActive(false); attendanceRepository.save(a); });
    }

    private void notifyShift(LeaseVehicleAssignment assignment, com.feros.api.entity.LeaseShift shift,
                             StaffProfile staff, LocalDate workDate) {
        if (staff == null) return;
        notificationService.sendToUser(assignment.getLease().getTenant(), staff.getUser(),
                NotificationType.LEASE_DRIVER_ASSIGNED,
                "Shift Assigned to You",
                "Shift " + shift.getShiftNumber() + " · " + shift.getStartTime() + "–" + shift.getEndTime()
                        + " on " + assignment.getVehicle().getRegistrationNumber() + ", " + workDate + ".");
    }

    private com.feros.api.dto.response.LeaseShiftAssignmentResponse toShiftAssignmentResponse(LeaseShiftAssignment a) {
        return com.feros.api.dto.response.LeaseShiftAssignmentResponse.builder()
                .id(a.getId())
                .assignmentId(a.getLeaseVehicleAssignment().getId())
                .vehicleId(a.getLeaseVehicleAssignment().getVehicle().getId())
                .registrationNumber(a.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber())
                .shiftId(a.getLeaseShift().getId())
                .shiftNumber(a.getLeaseShift().getShiftNumber())
                .startTime(a.getLeaseShift().getStartTime())
                .endTime(a.getLeaseShift().getEndTime())
                .workDate(a.getWorkDate())
                .driverStaffId(a.getDriverStaff() != null ? a.getDriverStaff().getUser().getId() : null)
                .driverName(a.getDriverStaff() != null ? a.getDriverStaff().getUser().getName() : null)
                .clientDriver(a.getDriverStaff() == null)
                .cleanerStaffId(a.getCleanerStaff() != null ? a.getCleanerStaff().getUser().getId() : null)
                .cleanerName(a.getCleanerStaff() != null ? a.getCleanerStaff().getUser().getName() : null)
                .clientCleaner(a.getCleanerStaff() == null)
                .driverPay(a.getDriverPaySnapshot())
                .cleanerPay(a.getCleanerPaySnapshot())
                .build();
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse addVehicle(Long leaseId, LeaseVehicleAssignmentRequest request) {
        VehicleLease lease = fetchLease(leaseId);
        if (lease.getStatus() == LeaseStatus.CLOSED)
            throw new FerosException("Cannot add vehicle to a closed lease", HttpStatus.BAD_REQUEST);

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleStatusType statusType = vehicle.getCurrentStatus() != null
                ? vehicle.getCurrentStatus().getStatusType() : null;

        // Block ON_TRIP vehicles — session in progress
        if (statusType == VehicleStatusType.ON_TRIP) {
            throw new FerosException("Vehicle is currently on a trip and cannot be reassigned", HttpStatus.BAD_REQUEST);
        }

        // If vehicle is on an active order (ALLOCATED/LR_CREATED) — unassign it first
        if (statusType != VehicleStatusType.ON_LEASE) {
            List<com.feros.api.entity.OrderVehicleAllocation> activeOrderAllocs =
                    orderVehicleAllocationRepository.findCurrentActiveAllocationsForVehicle(vehicle.getId());
            for (com.feros.api.entity.OrderVehicleAllocation ova : activeOrderAllocs) {
                if (ova.getAllocationStatus() == VehicleAllocationStatus.IN_TRANSIT)
                    throw new FerosException("Vehicle is currently in transit and cannot be reassigned", HttpStatus.BAD_REQUEST);
                // Cancel linked staff allocations
                List<com.feros.api.entity.OrderStaffAllocation> staffAllocs =
                        orderStaffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(ova.getId());
                for (com.feros.api.entity.OrderStaffAllocation sa : staffAllocs) {
                    sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                    sa.setIsActive(false);
                }
                orderStaffAllocationRepository.saveAll(staffAllocs);
                // Close open VSA for each staff member
                for (com.feros.api.entity.OrderStaffAllocation sa : staffAllocs) {
                    vehicleStaffAssignmentRepository
                            .findAllByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(
                                    sa.getUser().getId(), tenantId())
                            .forEach(vsa -> {
                                // Close the VSA (date it), but keep is_active=true so the staff
                                // still shows on the past days this assignment legitimately covered.
                                // is_active is not date-scoped — voiding it erases history.
                                vsa.setAssignedTo(LocalDate.now());
                                vsa.setUnassignedAt(LocalDateTime.now());
                            });
                }
                ova.setAllocationStatus(VehicleAllocationStatus.CANCELLED);
                ova.setIsActive(false);
                ova.setUnassignedAt(LocalDateTime.now());
                ova.setUnassignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null));
            }
            orderVehicleAllocationRepository.saveAll(activeOrderAllocs);
        }

        // Always close any existing active lease assignment — regardless of vehicle status
        assignmentRepository.findByLeaseIdAndVehicleIdActive(vehicle.getId()).ifPresent(existing -> {
            closeActiveSession(existing.getId(), LocalDateTime.now());
            existing.setIsActive(false);
            existing.setEndDate(LocalDate.now());
            leaseDriverLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(existing.getId())
                    .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));
            leaseCleanerLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(existing.getId())
                    .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));
            assignmentRepository.save(existing);
        });

        StaffProfile driver = null;
        if (request.getDriverStaffId() != null) {
            StaffProfile resolvedDriver = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getDriverStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));

            // Block if driver is already actively assigned to another lease vehicle
            leaseDriverLogRepository.findActiveByDriverStaffId(resolvedDriver.getId(), tenantId()).ifPresent(prevLog -> {
                String leaseNum = prevLog.getLeaseVehicleAssignment().getLease().getLeaseNumber();
                String vehNum = prevLog.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber();
                throw new FerosException(
                        resolvedDriver.getUser().getName() + " is already assigned to " + vehNum + " in lease " + leaseNum + ". Unassign them first.",
                        HttpStatus.CONFLICT);
            });
            driver = resolvedDriver;
        }

        StaffProfile cleaner = null;
        if (request.getCleanerStaffId() != null) {
            StaffProfile resolvedCleaner = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getCleanerStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Cleaner not found", HttpStatus.NOT_FOUND));

            // Block if cleaner is already actively assigned to another lease vehicle
            leaseCleanerLogRepository.findActiveByCleanerStaffId(resolvedCleaner.getId(), tenantId()).ifPresent(prevLog -> {
                String leaseNum = prevLog.getLeaseVehicleAssignment().getLease().getLeaseNumber();
                String vehNum = prevLog.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber();
                throw new FerosException(
                        resolvedCleaner.getUser().getName() + " is already assigned to " + vehNum + " in lease " + leaseNum + ". Unassign them first.",
                        HttpStatus.CONFLICT);
            });
            cleaner = resolvedCleaner;
        }

        LeaseVehicleAssignment assignment = LeaseVehicleAssignment.builder()
                .lease(lease)
                .vehicle(vehicle)
                .driverStaff(driver)
                .clientDriverName(driver == null ? request.getClientDriverName() : null)
                .cleanerStaff(cleaner)
                .clientCleanerName(cleaner == null ? request.getClientCleanerName() : null)
                .ratePerVehicle(request.getRatePerVehicle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .odometerAtStart(request.getOdometerAtStart())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            setVehicleOnLease(vehicle);
            vehicleRepository.save(vehicle);
        }

        LeaseVehicleAssignment saved = assignmentRepository.save(assignment);

        // Write initial driver log if driver provided
        if (driver != null) {
            leaseDriverLogRepository.save(LeaseDriverAssignmentLog.builder()
                    .leaseVehicleAssignment(saved)
                    .driverStaff(driver)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null))
                    .tenant(tenant())
                    .build());
        }

        // Write initial cleaner log if cleaner provided
        if (cleaner != null) {
            leaseCleanerLogRepository.save(LeaseCleanerAssignmentLog.builder()
                    .leaseVehicleAssignment(saved)
                    .cleanerStaff(cleaner)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null))
                    .tenant(tenant())
                    .build());
        }

        return toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse assignDriver(Long leaseId, Long assignmentId, AssignDriverRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        // Block driver change if session is currently active
        sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId).ifPresent(s -> {
            throw new FerosException("End the active session before changing the driver.", HttpStatus.CONFLICT);
        });

        // Close previous driver log entry
        leaseDriverLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(assignmentId)
                .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));

        StaffProfile driver = null;
        if (request.getDriverStaffId() == null) {
            assignment.setDriverStaff(null);
            assignment.setClientDriverName(request.getClientDriverName());
        } else {
            final StaffProfile resolvedDriver = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getDriverStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));

            Long driverUserId = resolvedDriver.getUser().getId();
            String driverName = resolvedDriver.getUser().getName();
            Long leaseVehicleId = assignment.getVehicle().getId();

            // One-vehicle rule (cross-system): require attendance today, never pull a driver off live
            // work, and either swap them off their current normal/order vehicle or block.
            if (!attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndApprovalStatusInAndIsActiveTrue(
                    driverUserId, tenantId(), TimeUtil.today(),
                    List.of(AttendanceApprovalStatus.PENDING, AttendanceApprovalStatus.APPROVED))) {
                throw new FerosException(driverName + " has not marked attendance today and cannot be assigned.",
                        HttpStatus.BAD_REQUEST);
            }
            staffAssignmentGuard.assertNotInProgress(driverUserId, tenantId(), driverName);
            if (request.isSwap()) {
                staffAssignmentGuard.releaseNormalAndOrder(driverUserId, tenantId(),
                        userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null), leaseVehicleId);
            } else {
                staffAssignmentGuard.assertNotOnNormalOrOrder(driverUserId, tenantId(), leaseVehicleId, driverName);
            }

            // Block if driver is already actively assigned to a different lease vehicle
            leaseDriverLogRepository.findActiveByDriverStaffId(resolvedDriver.getId(), tenantId()).ifPresent(prevLog -> {
                if (!prevLog.getLeaseVehicleAssignment().getId().equals(assignmentId)) {
                    String leaseNum = prevLog.getLeaseVehicleAssignment().getLease().getLeaseNumber();
                    String vehNum = prevLog.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber();
                    throw new FerosException(
                            resolvedDriver.getUser().getName() + " is already assigned to " + vehNum + " in lease " + leaseNum + ". Unassign them first.",
                            HttpStatus.CONFLICT);
                }
            });

            driver = resolvedDriver;
            assignment.setDriverStaff(driver);
            assignment.setClientDriverName(null);
        }
        LeaseVehicleAssignment saved = assignmentRepository.save(assignment);

        // Write new driver log entry (only when a real driver is assigned, not client's driver)
        if (driver != null) {
            leaseDriverLogRepository.save(LeaseDriverAssignmentLog.builder()
                    .leaseVehicleAssignment(saved)
                    .driverStaff(driver)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null))
                    .tenant(tenant())
                    .build());

            notificationService.sendToUser(saved.getLease().getTenant(), driver.getUser(),
                    NotificationType.LEASE_DRIVER_ASSIGNED,
                    "Vehicle Assigned to You",
                    "You have been assigned to " + saved.getVehicle().getRegistrationNumber()
                            + " for lease " + saved.getLease().getLeaseNumber() + ".");
        }
        return toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse assignCleaner(Long leaseId, Long assignmentId, AssignCleanerRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        // Block cleaner change if session is currently active
        sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId).ifPresent(s -> {
            throw new FerosException("End the active session before changing the cleaner.", HttpStatus.CONFLICT);
        });

        // Close previous cleaner log entry
        leaseCleanerLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(assignmentId)
                .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));

        StaffProfile cleaner = null;
        if (request.getCleanerStaffId() == null) {
            assignment.setCleanerStaff(null);
            assignment.setClientCleanerName(request.getClientCleanerName());
        } else {
            final StaffProfile resolvedCleaner = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getCleanerStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Cleaner not found", HttpStatus.NOT_FOUND));

            Long cleanerUserId = resolvedCleaner.getUser().getId();
            String cleanerName = resolvedCleaner.getUser().getName();
            Long leaseVehicleId = assignment.getVehicle().getId();

            // One-vehicle rule (cross-system): require attendance today, never pull a cleaner off live
            // work, and either swap them off their current normal/order vehicle or block.
            if (!attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndApprovalStatusInAndIsActiveTrue(
                    cleanerUserId, tenantId(), TimeUtil.today(),
                    List.of(AttendanceApprovalStatus.PENDING, AttendanceApprovalStatus.APPROVED))) {
                throw new FerosException(cleanerName + " has not marked attendance today and cannot be assigned.",
                        HttpStatus.BAD_REQUEST);
            }
            staffAssignmentGuard.assertNotInProgress(cleanerUserId, tenantId(), cleanerName);
            if (request.isSwap()) {
                staffAssignmentGuard.releaseNormalAndOrder(cleanerUserId, tenantId(),
                        userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null), leaseVehicleId);
            } else {
                staffAssignmentGuard.assertNotOnNormalOrOrder(cleanerUserId, tenantId(), leaseVehicleId, cleanerName);
            }

            // Block if cleaner is already actively assigned to a different lease vehicle
            leaseCleanerLogRepository.findActiveByCleanerStaffId(resolvedCleaner.getId(), tenantId()).ifPresent(prevLog -> {
                if (!prevLog.getLeaseVehicleAssignment().getId().equals(assignmentId)) {
                    String leaseNum = prevLog.getLeaseVehicleAssignment().getLease().getLeaseNumber();
                    String vehNum = prevLog.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber();
                    throw new FerosException(
                            resolvedCleaner.getUser().getName() + " is already assigned to " + vehNum + " in lease " + leaseNum + ". Unassign them first.",
                            HttpStatus.CONFLICT);
                }
            });

            cleaner = resolvedCleaner;
            assignment.setCleanerStaff(cleaner);
            assignment.setClientCleanerName(null);
        }
        LeaseVehicleAssignment saved = assignmentRepository.save(assignment);

        // Write new cleaner log entry (only when a real cleaner is assigned, not client's cleaner)
        if (cleaner != null) {
            leaseCleanerLogRepository.save(LeaseCleanerAssignmentLog.builder()
                    .leaseVehicleAssignment(saved)
                    .cleanerStaff(cleaner)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null))
                    .tenant(tenant())
                    .build());

            notificationService.sendToUser(saved.getLease().getTenant(), cleaner.getUser(),
                    NotificationType.LEASE_CLEANER_ASSIGNED,
                    "Vehicle Assigned to You",
                    "You have been assigned to " + saved.getVehicle().getRegistrationNumber()
                            + " for lease " + saved.getLease().getLeaseNumber() + ".");
        }
        return toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse assignDivision(Long leaseId, Long assignmentId, AssignDivisionRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        if (request.getDivisionId() == null) {
            assignment.setDivisionId(null);
            assignment.setDivisionName(null);
        } else {
            ClientDivision division = clientDivisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new FerosException("Division not found", HttpStatus.NOT_FOUND));
            assignment.setDivisionId(division.getId());
            assignment.setDivisionName(division.getName());
        }
        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse closeVehicleAssignment(Long leaseId, Long assignmentId, BigDecimal odometerAtEnd) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        if (sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId).isPresent())
            throw new FerosException("Cannot close vehicle — driver has an active session in progress. End the session first.", HttpStatus.CONFLICT);

        // Close driver + cleaner assignment logs so their dashboards no longer show On Lease
        leaseDriverLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(assignmentId)
                .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));
        leaseCleanerLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(assignmentId)
                .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));

        // Shift-duty leases: drop this vehicle's shift assignments from today forward so the
        // closed vehicle can never pin its driver/cleaner or block reassignment. Past days
        // stay for payroll. Mirrors the whole-lease close (updateStatus → CLOSED).
        leaseShiftAssignmentRepository.deleteAll(
                leaseShiftAssignmentRepository.findByLeaseVehicleAssignmentIdAndWorkDateGreaterThanEqual(
                        assignmentId, LocalDate.now()));

        assignment.setIsActive(false);
        assignment.setEndDate(LocalDate.now());
        assignment.setOdometerAtEnd(odometerAtEnd);

        revertVehicleStatus(assignment.getVehicle());
        vehicleRepository.save(assignment.getVehicle());

        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    // ── Billing ───────────────────────────────────────────────────────────────

    @Override
    public LeaseBillingResponse getBilling(Long leaseId) {
        VehicleLease lease = fetchLease(leaseId);
        List<LeaseVehicleAssignment> assignments = assignmentRepository.findByLeaseIdOrderByStartDateAsc(leaseId);

        LocalDate today = LocalDate.now();

        List<LeaseBillingResponse.VehicleBillingLine> lines = assignments.stream().map(a -> {
            LocalDate from = a.getStartDate();
            LocalDate to = a.getEndDate() != null ? a.getEndDate() : today;
            long days = Math.max(ChronoUnit.DAYS.between(from, to), 1);
            BigDecimal amount = calculateAmount(lease.getRateType(), days, a.getRatePerVehicle());
            return LeaseBillingResponse.VehicleBillingLine.builder()
                    .assignmentId(a.getId())
                    .registrationNumber(a.getVehicle().getRegistrationNumber())
                    .ratePerVehicle(a.getRatePerVehicle())
                    .days(days)
                    .amount(amount)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = lines.stream()
                .map(LeaseBillingResponse.VehicleBillingLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LeaseBillingResponse.builder()
                .leaseId(lease.getId())
                .leaseNumber(lease.getLeaseNumber())
                .rateType(lease.getRateType())
                .lines(lines)
                .totalAmount(total)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal calculateAmount(RateType rateType, long days, BigDecimal rate) {
        if (rateType == RateType.MONTHLY) {
            // Prorate: days / 30
            return rate.multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        }
        // PER_DAY (DAILY_SHIFT mapped to per day)
        return rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    private void setVehicleOnLease(Vehicle vehicle) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.ON_LEASE)
                .ifPresent(vehicle::setCurrentStatus);

        // A leased vehicle is staffed from the lease — release any standing normal driver/cleaner so
        // it no longer shows them as the current driver/cleaner, and frees them to be reassigned.
        var actor = userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null);
        java.util.stream.Stream.of(vehicle.getCurrentDriver(), vehicle.getCurrentCleaner())
                .filter(java.util.Objects::nonNull)
                .forEach(staff -> vehicleStaffAssignmentRepository
                        .findAllByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(staff.getId(), tenantId())
                        .stream()
                        .filter(vsa -> vsa.getVehicle().getId().equals(vehicle.getId()))
                        .forEach(vsa -> {
                            // Close the VSA (date it), but keep is_active=true — voiding it would
                            // retroactively remove the staff from every past day it covered.
                            vsa.setAssignedTo(LocalDate.now());
                            vsa.setUnassignedBy(actor);
                            vsa.setUnassignedAt(LocalDateTime.now());
                        }));
        vehicle.setCurrentDriver(null);
        vehicle.setCurrentCleaner(null);
    }

    private void revertVehicleStatus(Vehicle vehicle) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.AVAILABLE)
                .ifPresent(vehicle::setCurrentStatus);
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaseVehicleSessionResponse startSession(Long leaseId, Long assignmentId, LeaseSessionStartRequest request) {
        VehicleLease lease = fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(assignment.getIsActive()))
            throw new FerosException("Cannot start session on a closed assignment", HttpStatus.BAD_REQUEST);

        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        if (startTime.isAfter(LocalDateTime.now()))
            throw new FerosException("Start time cannot be in the future", HttpStatus.BAD_REQUEST);

        // When called by a DRIVER — verify they're assigned to this vehicle TODAY, then record them.
        // Shift leases have no vehicle-level driver, so validate against today's shift crew (handover model:
        // any of today's shift drivers on this vehicle may start; the active session auto-closes below).
        Long resolvedDriverStaffId = request.getDriverStaffId();
        if ("DRIVER".equals(SecurityUtil.getCurrentRole())) {
            StaffProfile self = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(SecurityUtil.getCurrentUserId(), tenantId())
                    .orElseThrow(() -> new FerosException("Staff profile not found", HttpStatus.NOT_FOUND));
            boolean ok = Boolean.TRUE.equals(lease.getShiftDuty())
                    ? leaseShiftAssignmentRepository.findByDriverStaffIdAndWorkDate(self.getId(), TimeUtil.today())
                            .stream().anyMatch(a -> a.getLeaseVehicleAssignment().getId().equals(assignmentId)
                                    && shiftCoversNow(a.getLeaseShift()))
                    : assignment.getDriverStaff() != null && assignment.getDriverStaff().getId().equals(self.getId());
            if (!ok)
                throw new FerosException(
                        Boolean.TRUE.equals(lease.getShiftDuty())
                                ? "You can only start a session during your shift window."
                                : "You are not assigned to this vehicle today.",
                        HttpStatus.FORBIDDEN);
            resolvedDriverStaffId = SecurityUtil.getCurrentUserId();
        }

        // Odometer start: request → last closed session's odometerEnd → assignment's odometerAtStart
        BigDecimal odometerStart = request.getOdometerStart();
        if (odometerStart == null) {
            odometerStart = sessionRepository
                    .findFirstByAssignmentIdAndIsActiveFalseAndOdometerEndNotNullOrderByEndTimeDesc(assignmentId)
                    .map(LeaseVehicleSession::getOdometerEnd)
                    .orElse(assignment.getOdometerAtStart());
        }
        // Handover: close any active session, carrying the odometer forward for continuity
        closeActiveSession(assignmentId, startTime, odometerStart);

        // Resolve driver name if own staff
        String driverName = null;
        if (resolvedDriverStaffId != null) {
            StaffProfile driver = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(resolvedDriverStaffId, tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));
            driverName = driver.getUser().getName();
        }

        // Resolve division name — drivers use the assignment's current division, cannot change it
        String divisionName = null;
        Long divisionId = null;
        if ("DRIVER".equals(SecurityUtil.getCurrentRole())) {
            divisionId = assignment.getDivisionId();
            divisionName = assignment.getDivisionName();
        } else if (request.getDivisionId() != null) {
            ClientDivision division = clientDivisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new FerosException("Division not found", HttpStatus.NOT_FOUND));
            divisionId = division.getId();
            divisionName = division.getName();
        }

        LeaseVehicleSession session = LeaseVehicleSession.builder()
                .assignment(assignment)
                .driverStaffId(resolvedDriverStaffId)
                .driverName(driverName)
                .divisionId(divisionId)
                .divisionName(divisionName)
                .odometerStart(odometerStart)
                .startTime(startTime)
                .isActive(true)
                .notes(request.getNotes())
                .build();

        LeaseVehicleSession saved = sessionRepository.save(session);

        // Push odometerStart back to vehicle master
        if (odometerStart != null) {
            Vehicle v = assignment.getVehicle();
            if (v.getCurrentOdometerReading() == null || odometerStart.compareTo(v.getCurrentOdometerReading()) > 0) {
                v.setCurrentOdometerReading(odometerStart);
                vehicleRepository.save(v);
            }
        }

        return toSessionResponse(saved);
    }

    @Override
    @Transactional
    public LeaseVehicleSessionResponse endSession(Long leaseId, Long assignmentId, LocalDateTime endTime, BigDecimal odometerEnd, String notes) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        LeaseVehicleSession session = sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId)
                .orElseThrow(() -> new FerosException("No active session found for this vehicle", HttpStatus.NOT_FOUND));

        if ("DRIVER".equals(SecurityUtil.getCurrentRole())) {
            StaffProfile self = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(SecurityUtil.getCurrentUserId(), tenantId())
                    .orElseThrow(() -> new FerosException("Staff profile not found", HttpStatus.NOT_FOUND));
            if (!isAssignedDriver(assignment, session, self))
                throw new FerosException("You are not the assigned driver for this vehicle.", HttpStatus.FORBIDDEN);
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        if (end.isAfter(LocalDateTime.now()))
            throw new FerosException("End time cannot be in the future", HttpStatus.BAD_REQUEST);
        if (end.isBefore(session.getStartTime()))
            throw new FerosException("End time must be after start time", HttpStatus.BAD_REQUEST);

        session.setEndTime(end);
        session.setHoursWorked(computeHours(session.getStartTime(), end));
        session.setIsActive(false);
        if (odometerEnd != null) {
            session.setOdometerEnd(odometerEnd);
            if (session.getOdometerStart() != null)
                session.setKmDriven(odometerEnd.subtract(session.getOdometerStart()).max(BigDecimal.ZERO));
            // Push back to vehicle master
            Vehicle v = assignment.getVehicle();
            if (v.getCurrentOdometerReading() == null || odometerEnd.compareTo(v.getCurrentOdometerReading()) > 0) {
                v.setCurrentOdometerReading(odometerEnd);
                vehicleRepository.save(v);
            }
        }
        if (notes != null) session.setNotes(notes);

        LeaseVehicleSession savedSession = sessionRepository.save(session);
        // Recompute the daily log for the session's START date so a night session (started today,
        // closed tomorrow) rolls into TODAY — the 23:59 scheduler misses it because it's still active.
        recomputeDailyLog(assignment, savedSession.getStartTime().toLocalDate());
        return toSessionResponse(savedSession);
    }

    /** True if this driver owns the session: for shift-duty leases that means a shift assignment on
     *  the session's start date for this vehicle; otherwise the standing lease driver. */
    private boolean isAssignedDriver(LeaseVehicleAssignment assignment, LeaseVehicleSession session, StaffProfile self) {
        if (Boolean.TRUE.equals(assignment.getLease().getShiftDuty())) {
            // Shift lease: only the driver who STARTED this session may end it (not any shift driver that
            // day) — otherwise an off-shift driver could end the current shift driver's running session.
            return session.getDriverStaffId() != null
                    && session.getDriverStaffId().equals(self.getUser().getId());
        }
        return assignment.getDriverStaff() != null && assignment.getDriverStaff().getId().equals(self.getId());
    }

    /** Upsert the one-per-day roll-up from this assignment's completed sessions started on logDate. */
    private void recomputeDailyLog(LeaseVehicleAssignment assignment, LocalDate logDate) {
        List<LeaseVehicleSession> sessions = sessionRepository
                .findByAssignmentIdAndIsActiveFalseAndStartTimeBetween(
                        assignment.getId(), logDate.atStartOfDay(), logDate.plusDays(1).atStartOfDay());
        if (sessions.isEmpty()) return;
        BigDecimal totalHours = sessions.stream()
                .map(s -> s.getHoursWorked() != null ? s.getHoursWorked() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal kmDriven = sessions.stream()
                .map(s -> s.getKmDriven() != null ? s.getKmDriven() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        LeaseDailyLog log = dailyLogRepository
                .findByAssignmentIdAndLogDate(assignment.getId(), logDate)
                .orElseGet(() -> LeaseDailyLog.builder()
                        .assignment(assignment).leaseId(assignment.getLease().getId())
                        .logDate(logDate).source("AUTO").build());
        log.setTotalHours(totalHours.compareTo(BigDecimal.ZERO) > 0 ? totalHours : null);
        log.setKmDriven(kmDriven.compareTo(BigDecimal.ZERO) > 0 ? kmDriven : null);
        log.setSessionCount(sessions.size());
        if (log.getNotes() == null) log.setNotes("Auto-updated from " + sessions.size() + " session(s)");
        dailyLogRepository.save(log);
    }

    @Override
    public List<LeaseVehicleSessionResponse> getSessions(Long leaseId, Long assignmentId) {
        fetchLease(leaseId);
        List<LeaseVehicleSession> sessions = assignmentId != null
                ? sessionRepository.findByAssignmentIdOrderByStartTimeDesc(assignmentId)
                : sessionRepository.findByAssignment_Lease_IdOrderByStartTimeDesc(leaseId);
        return sessions.stream().map(this::toSessionResponse).collect(Collectors.toList());
    }

    // ── Daily Logs ────────────────────────────────────────────────────────────

    @Override
    public List<LeaseDailyLogResponse> getDailyLogs(Long leaseId) {
        fetchLease(leaseId);
        return dailyLogRepository.findByLeaseIdOrderByLogDateDesc(leaseId)
                .stream().map(this::toDailyLogResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaseDailyLogResponse createDailyLog(Long leaseId, Long assignmentId, LocalDate date) {
        VehicleLease lease = fetchLease(leaseId);
        if (lease.getStatus() == LeaseStatus.CLOSED)
            throw new FerosException("Cannot add daily log to a closed lease", HttpStatus.BAD_REQUEST);

        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        if (date.isAfter(LocalDate.now()))
            throw new FerosException("Cannot create daily log for a future date", HttpStatus.BAD_REQUEST);
        if (dailyLogRepository.existsByAssignmentIdAndLogDate(assignmentId, date))
            throw new FerosException("Daily log already exists for this vehicle on " + date, HttpStatus.CONFLICT);

        List<LeaseVehicleSession> sessions = sessionRepository
                .findByAssignmentIdAndIsActiveFalseAndStartTimeBetween(
                        assignmentId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        if (sessions.isEmpty())
            throw new FerosException("No completed sessions found for this vehicle on " + date, HttpStatus.BAD_REQUEST);

        LeaseDailyLog log = dailyLogRepository.save(buildDailyLog(assignment, date, sessions, "MANUAL"));
        return toDailyLogResponse(log);
    }

    private LeaseDailyLog buildDailyLog(LeaseVehicleAssignment assignment, LocalDate date,
                                         List<LeaseVehicleSession> sessions, String source) {
        BigDecimal totalHours = sessions.stream()
                .map(s -> s.getHoursWorked() != null ? s.getHoursWorked() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal kmDriven = sessions.stream()
                .map(s -> s.getKmDriven() != null ? s.getKmDriven() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return LeaseDailyLog.builder()
                .assignment(assignment)
                .leaseId(assignment.getLease().getId())
                .logDate(date)
                .totalHours(totalHours.compareTo(BigDecimal.ZERO) > 0 ? totalHours : null)
                .kmDriven(kmDriven.compareTo(BigDecimal.ZERO) > 0 ? kmDriven : null)
                .sessionCount(sessions.size())
                .source(source)
                .build();
    }

    private LeaseDailyLogResponse toDailyLogResponse(LeaseDailyLog log) {
        return LeaseDailyLogResponse.builder()
                .id(log.getId())
                .assignmentId(log.getAssignment().getId())
                .leaseId(log.getLeaseId())
                .logDate(log.getLogDate())
                .registrationNumber(log.getAssignment().getVehicle().getRegistrationNumber())
                .totalHours(log.getTotalHours())
                .kmDriven(log.getKmDriven())
                .sessionCount(log.getSessionCount())
                .source(log.getSource())
                .notes(log.getNotes())
                .createdAt(log.getCreatedAt())
                .build();
    }

    // Close the active session for an assignment at the given time
    private void closeActiveSession(Long assignmentId, LocalDateTime at) {
        closeActiveSession(assignmentId, at, null);
    }

    /** Close the active session; on a handover carry the new session's start odometer onto the old
     *  session's end so the vehicle's distance stays continuous. */
    private void closeActiveSession(Long assignmentId, LocalDateTime at, BigDecimal odometerEnd) {
        sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId).ifPresent(s -> {
            s.setEndTime(at);
            s.setHoursWorked(computeHours(s.getStartTime(), at));
            if (odometerEnd != null && s.getOdometerEnd() == null) s.setOdometerEnd(odometerEnd);
            s.setIsActive(false);
            sessionRepository.save(s);
        });
    }

    private BigDecimal computeHours(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private LeaseVehicleSessionResponse toSessionResponse(LeaseVehicleSession s) {
        String regNum = s.getAssignment().getVehicle().getRegistrationNumber();
        Long vehicleId = s.getAssignment().getVehicle().getId();
        return LeaseVehicleSessionResponse.builder()
                .id(s.getId())
                .assignmentId(s.getAssignment().getId())
                .vehicleId(vehicleId)
                .registrationNumber(regNum)
                .driverStaffId(s.getDriverStaffId())
                .driverName(s.getDriverName())
                .divisionId(s.getDivisionId())
                .divisionName(s.getDivisionName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .odometerStart(s.getOdometerStart())
                .odometerEnd(s.getOdometerEnd())
                .kmDriven(s.getKmDriven())
                .hoursWorked(s.getHoursWorked())
                .isActive(Boolean.TRUE.equals(s.getIsActive()))
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private VehicleLeaseResponse toResponse(VehicleLease lease, long vehicleCount) {
        List<com.feros.api.dto.response.LeaseShiftResponse> shifts =
                Boolean.TRUE.equals(lease.getShiftDuty())
                        ? leaseShiftRepository.findByLeaseIdOrderByShiftNumberAsc(lease.getId()).stream()
                                .map(s -> com.feros.api.dto.response.LeaseShiftResponse.builder()
                                        .id(s.getId())
                                        .shiftNumber(s.getShiftNumber())
                                        .startTime(s.getStartTime())
                                        .endTime(s.getEndTime())
                                        .driverPay(s.getDriverPay())
                                        .cleanerPay(s.getCleanerPay())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of();
        return VehicleLeaseResponse.builder()
                .id(lease.getId())
                .tenantId(lease.getTenant().getId())
                .leaseNumber(lease.getLeaseNumber())
                .clientId(lease.getClient().getId())
                .clientName(lease.getClient().getClientName())
                .site(lease.getSite())
                .startDate(lease.getStartDate())
                .endDate(lease.getEndDate())
                .rateType(lease.getRateType())
                .status(lease.getStatus())
                .notes(lease.getNotes())
                .shiftDuty(Boolean.TRUE.equals(lease.getShiftDuty()))
                .shifts(shifts)
                .vehicleCount(vehicleCount)
                .createdAt(lease.getCreatedAt())
                .updatedAt(lease.getUpdatedAt())
                .build();
    }

    // ── Shift definitions ───────────────────────────────────────────────────────

    private static final int MAX_SHIFTS = 4;

    /** Replace the lease's shift windows from the request. Validates count (≤4), non-overlap
     *  (midnight-crossing allowed), and non-negative pay. No-op shift list when shiftDuty is off. */
    private void persistShifts(VehicleLease lease, VehicleLeaseRequest request) {
        leaseShiftRepository.deleteByLeaseId(lease.getId());
        if (!Boolean.TRUE.equals(lease.getShiftDuty())) return;

        List<ShiftDefinitionRequest> defs = request.getShifts();
        if (defs == null || defs.isEmpty())
            throw new FerosException("A shift-duty lease needs at least one shift.", HttpStatus.BAD_REQUEST);
        if (defs.size() > MAX_SHIFTS)
            throw new FerosException("A lease can have at most " + MAX_SHIFTS + " shifts.", HttpStatus.BAD_REQUEST);

        for (ShiftDefinitionRequest d : defs) {
            if (d.getStartTime().equals(d.getEndTime()))
                throw new FerosException("Shift start and end time cannot be the same.", HttpStatus.BAD_REQUEST);
            if (d.getDriverPay() == null || d.getDriverPay().signum() < 0)
                throw new FerosException("Shift driver pay must be zero or more.", HttpStatus.BAD_REQUEST);
            if (d.getCleanerPay() != null && d.getCleanerPay().signum() < 0)
                throw new FerosException("Shift cleaner pay must be zero or more.", HttpStatus.BAD_REQUEST);
        }
        assertNoOverlap(defs);

        Tenant t = lease.getTenant();
        for (ShiftDefinitionRequest d : defs) {
            leaseShiftRepository.save(com.feros.api.entity.LeaseShift.builder()
                    .lease(lease)
                    .shiftNumber(d.getShiftNumber())
                    .startTime(d.getStartTime())
                    .endTime(d.getEndTime())
                    .driverPay(d.getDriverPay())
                    .cleanerPay(d.getCleanerPay())
                    .tenant(t)
                    .build());
        }
    }

    /** Two shift windows overlap if their minute-of-day intervals intersect. A window with
     *  start > end wraps midnight and becomes two intervals: [start,1440) and [0,end). */
    public static void assertNoOverlap(List<ShiftDefinitionRequest> defs) {
        List<int[]> intervals = new java.util.ArrayList<>();
        for (ShiftDefinitionRequest d : defs) {
            int s = d.getStartTime().toSecondOfDay() / 60;
            int e = d.getEndTime().toSecondOfDay() / 60;
            if (s < e) {
                intervals.add(new int[]{s, e});
            } else { // wraps midnight
                intervals.add(new int[]{s, 1440});
                intervals.add(new int[]{0, e});
            }
        }
        for (int i = 0; i < intervals.size(); i++) {
            for (int j = i + 1; j < intervals.size(); j++) {
                int[] a = intervals.get(i), b = intervals.get(j);
                if (a[0] < b[1] && b[0] < a[1])
                    throw new FerosException("Shift time windows cannot overlap.", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private LeaseVehicleAssignmentResponse toAssignmentResponse(LeaseVehicleAssignment a) {
        String driverName = a.getDriverStaff() != null ? a.getDriverStaff().getUser().getName() : null;
        Long driverStaffId = a.getDriverStaff() != null ? a.getDriverStaff().getUser().getId() : null;
        String cleanerName = a.getCleanerStaff() != null ? a.getCleanerStaff().getUser().getName() : null;
        Long cleanerStaffId = a.getCleanerStaff() != null ? a.getCleanerStaff().getUser().getId() : null;
        String vehicleType = a.getVehicle().getVehicleType() != null
                ? a.getVehicle().getVehicleType().getName() : null;

        return LeaseVehicleAssignmentResponse.builder()
                .id(a.getId())
                .leaseId(a.getLease().getId())
                .vehicleId(a.getVehicle().getId())
                .registrationNumber(a.getVehicle().getRegistrationNumber())
                .vehicleType(vehicleType)
                .driverStaffId(driverStaffId)
                .driverName(driverName)
                .clientDriverName(a.getClientDriverName())
                .cleanerStaffId(cleanerStaffId)
                .cleanerName(cleanerName)
                .clientCleanerName(a.getClientCleanerName())
                .ratePerVehicle(a.getRatePerVehicle())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .odometerAtStart(a.getOdometerAtStart())
                .odometerAtEnd(a.getOdometerAtEnd())
                .vehicleCurrentOdometer(a.getVehicle().getCurrentOdometerReading())
                .divisionId(a.getDivisionId())
                .divisionName(a.getDivisionName())
                .isActive(Boolean.TRUE.equals(a.getIsActive()))
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
