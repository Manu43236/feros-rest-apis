package com.feros.api.service.impl;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.LeaseSessionStartRequest;
import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
import com.feros.api.dto.request.VehicleLeaseRequest;
import com.feros.api.dto.response.LeaseBillingResponse;
import com.feros.api.dto.response.LeaseVehicleAssignmentResponse;
import com.feros.api.dto.response.LeaseVehicleSessionResponse;
import com.feros.api.dto.response.VehicleLeaseResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.LeaseStatus;
import com.feros.api.enums.RateType;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
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
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final ClientDivisionRepository clientDivisionRepository;

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
                .leaseNumber(NumberUtil.generate(t.getPrefix(), t.getId(), NumberUtil.Type.LSE))
                .client(client)
                .site(request.getSite())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rateType(request.getRateType())
                .notes(request.getNotes())
                .build();

        return toResponse(leaseRepository.save(lease), 0);
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

        if (assignmentRepository.existsActiveLeaseForVehicle(vehicle.getId()))
            throw new FerosException("Vehicle is already on an active lease", HttpStatus.BAD_REQUEST);

        StaffProfile driver = null;
        if (request.getDriverStaffId() != null) {
            driver = staffProfileRepository.findByIdAndTenantId(request.getDriverStaffId(), tenantId())
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

        // Mark vehicle as On Lease if lease is active
        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            setVehicleOnLease(vehicle);
            vehicleRepository.save(vehicle);
        }

        return toAssignmentResponse(assignmentRepository.save(assignment));
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
            StaffProfile driver = staffProfileRepository.findByIdAndTenantId(request.getDriverStaffId(), tenantId())
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

        LeaseVehicleSession session = LeaseVehicleSession.builder()
                .assignment(assignment)
                .driverStaffId(request.getDriverStaffId())
                .driverName(driverName)
                .divisionId(divisionId)
                .divisionName(divisionName)
                .startTime(startTime)
                .isActive(true)
                .notes(request.getNotes())
                .build();

        return toSessionResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public LeaseVehicleSessionResponse endSession(Long leaseId, Long assignmentId, LocalDateTime endTime, String notes) {
        fetchLease(leaseId);
        assignmentRepository.findByIdAndLeaseId(assignmentId, leaseId)
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
        Long driverStaffId = a.getDriverStaff() != null ? a.getDriverStaff().getId() : null;
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
                .divisionId(a.getDivisionId())
                .divisionName(a.getDivisionName())
                .isActive(Boolean.TRUE.equals(a.getIsActive()))
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
