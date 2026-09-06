package com.feros.api.service.impl;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.AssignDriverRequest;
import com.feros.api.dto.request.LeaseSessionStartRequest;
import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
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
    private final OrderVehicleAllocationRepository orderVehicleAllocationRepository;
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
    private final UserRepository userRepository;

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
    public Page<VehicleLeaseResponse> getAll(int page, int size, LeaseStatus status, Long clientId) {
        return leaseRepository.findAllPaged(tenantId(), status, clientId, PageRequest.of(page, size))
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
                .build();

        VehicleLease saved = leaseRepository.save(lease);
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

        return toResponse(leaseRepository.save(lease), assignmentRepository.countByLeaseId(id));
    }

    // ── Status ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VehicleLeaseResponse updateStatus(Long id, LeaseStatus newStatus) {
        VehicleLease lease = fetchLease(id);

        validateStatusTransition(lease.getStatus(), newStatus);
        lease.setStatus(newStatus);

        // On close: close all active vehicle assignments, their sessions, and revert vehicle status
        if (newStatus == LeaseStatus.CLOSED) {
            List<LeaseVehicleAssignment> active = assignmentRepository
                    .findByLeaseIdOrderByStartDateAsc(id)
                    .stream().filter(a -> Boolean.TRUE.equals(a.getIsActive())).collect(Collectors.toList());
            LocalDateTime now = LocalDateTime.now();
            active.forEach(a -> {
                closeActiveSession(a.getId(), now);
                a.setIsActive(false);
                a.setEndDate(LocalDate.now());
                revertVehicleStatus(a.getVehicle());
            });
            assignmentRepository.saveAll(active);
        }

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
                                vsa.setAssignedTo(LocalDate.now());
                                vsa.setUnassignedAt(LocalDateTime.now());
                                vsa.setIsActive(false);
                            });
                }
                ova.setAllocationStatus(VehicleAllocationStatus.CANCELLED);
                ova.setIsActive(false);
                ova.setUnassignedAt(LocalDateTime.now());
                ova.setUnassignedBy(userRepository.findById(SecurityUtil.getCurrentUserId()).orElse(null));
            }
            orderVehicleAllocationRepository.saveAll(activeOrderAllocs);
        }

        // If vehicle is on an active lease — close that assignment first
        if (statusType == VehicleStatusType.ON_LEASE) {
            assignmentRepository.findByLeaseIdAndVehicleIdActive(vehicle.getId()).ifPresent(existing -> {
                closeActiveSession(existing.getId(), LocalDateTime.now());
                existing.setIsActive(false);
                existing.setEndDate(LocalDate.now());
                // Close open lease driver log
                leaseDriverLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(existing.getId())
                        .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));
                assignmentRepository.save(existing);
            });
        }

        StaffProfile driver = null;
        if (request.getDriverStaffId() != null) {
            driver = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getDriverStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));
        }

        LeaseVehicleAssignment assignment = LeaseVehicleAssignment.builder()
                .lease(lease)
                .vehicle(vehicle)
                .driverStaff(driver)
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

        return toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LeaseVehicleAssignmentResponse assignDriver(Long leaseId, Long assignmentId, AssignDriverRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        // Close previous driver log entry
        leaseDriverLogRepository.findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(assignmentId)
                .ifPresent(log -> log.setUnassignedAt(LocalDateTime.now()));

        StaffProfile driver = null;
        if (request.getDriverStaffId() == null) {
            assignment.setDriverStaff(null);
        } else {
            driver = staffProfileRepository.findByIdAndTenantId(request.getDriverStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));
            assignment.setDriverStaff(driver);
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

        closeActiveSession(assignmentId, LocalDateTime.now());

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
    }

    private void revertVehicleStatus(Vehicle vehicle) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.AVAILABLE)
                .ifPresent(vehicle::setCurrentStatus);
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaseVehicleSessionResponse startSession(Long leaseId, Long assignmentId, LeaseSessionStartRequest request) {
        fetchLease(leaseId);
        LeaseVehicleAssignment assignment = assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(assignment.getIsActive()))
            throw new FerosException("Cannot start session on a closed assignment", HttpStatus.BAD_REQUEST);

        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        if (startTime.isAfter(LocalDateTime.now()))
            throw new FerosException("Start time cannot be in the future", HttpStatus.BAD_REQUEST);

        // Close any currently active session
        closeActiveSession(assignmentId, startTime);

        // Resolve driver name if own staff
        String driverName = null;
        if (request.getDriverStaffId() != null) {
            StaffProfile driver = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(request.getDriverStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Driver not found", HttpStatus.NOT_FOUND));
            driverName = driver.getUser().getName();
        }

        // Resolve division name
        String divisionName = null;
        Long divisionId = null;
        if (request.getDivisionId() != null) {
            ClientDivision division = clientDivisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new FerosException("Division not found", HttpStatus.NOT_FOUND));
            divisionId = division.getId();
            divisionName = division.getName();
        }

        // Odometer start: use request value → last session's odometerEnd → assignment's odometerAtStart
        BigDecimal odometerStart = request.getOdometerStart();
        if (odometerStart == null) {
            odometerStart = sessionRepository
                    .findFirstByAssignmentIdAndIsActiveFalseAndOdometerEndNotNullOrderByEndTimeDesc(assignmentId)
                    .map(LeaseVehicleSession::getOdometerEnd)
                    .orElse(assignment.getOdometerAtStart());
        }

        LeaseVehicleSession session = LeaseVehicleSession.builder()
                .assignment(assignment)
                .driverStaffId(request.getDriverStaffId())
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

        return toSessionResponse(sessionRepository.save(session));
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
        fetchLease(leaseId);
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
        sessionRepository.findByAssignmentIdAndIsActiveTrue(assignmentId).ifPresent(s -> {
            s.setEndTime(at);
            s.setHoursWorked(computeHours(s.getStartTime(), at));
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
                .vehicleCount(vehicleCount)
                .createdAt(lease.getCreatedAt())
                .updatedAt(lease.getUpdatedAt())
                .build();
    }

    private LeaseVehicleAssignmentResponse toAssignmentResponse(LeaseVehicleAssignment a) {
        String driverName = a.getDriverStaff() != null ? a.getDriverStaff().getUser().getName() : null;
        Long driverStaffId = a.getDriverStaff() != null ? a.getDriverStaff().getUser().getId() : null;
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
