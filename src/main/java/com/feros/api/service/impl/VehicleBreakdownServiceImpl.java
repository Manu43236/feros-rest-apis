package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.BreakdownRequest;
import com.feros.api.dto.request.BreakdownReplaceRequest;
import com.feros.api.dto.response.BreakdownResponse;
import com.feros.api.dto.response.VehicleCurrentStaffResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.VehicleBreakdownService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleBreakdownServiceImpl implements VehicleBreakdownService {

    private final VehicleBreakdownRepository breakdownRepository;
    private final OrderVehicleAllocationRepository vehicleAllocationRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final OrderRepository orderRepository;
    private final LrRepository lrRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final VehicleMeterReadingRepository vehicleMeterReadingRepository;
    private final AttendanceRepository attendanceRepository;
    private final RoleRepository roleRepository;
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Long getTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    private void setVehicleStatus(Vehicle vehicle, VehicleStatusType type) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(type)
                .ifPresent(status -> {
                    vehicle.setCurrentStatus(status);
                    vehicleRepository.save(vehicle);
                });
    }

    // ── report breakdown (admin/supervisor/office-staff) ─────────────────────

    @Override
    @Transactional
    public BreakdownResponse reportBreakdown(Long orderId, Long allocationId, BreakdownRequest request) {
        Long tenantId = getTenantId();

        orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        OrderVehicleAllocation allocation = vehicleAllocationRepository
                .findByIdAndTenantIdAndIsActiveTrue(allocationId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle allocation not found", HttpStatus.NOT_FOUND));

        if (!allocation.getOrder().getId().equals(orderId)) {
            throw new FerosException("Vehicle allocation does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        return doReportBreakdown(allocation, request, tenantId);
    }

    // ── report breakdown (driver — auto-detects their active trip) ────────────

    @Override
    @Transactional
    public BreakdownResponse reportMyBreakdown(BreakdownRequest request) {
        Long tenantId = getTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        List<OrderStaffAllocation> active = staffAllocationRepository
                .findActiveAllocationsForUser(userId,
                        List.of(StaffAllocationStatus.IN_TRANSIT));

        if (active.isEmpty()) {
            throw new FerosException(
                    "No active trip found — you are not currently in transit",
                    HttpStatus.BAD_REQUEST);
        }

        OrderVehicleAllocation allocation = active.get(0).getVehicleAllocation();
        return doReportBreakdown(allocation, request, tenantId);
    }

    // ── shared breakdown creation logic ──────────────────────────────────────

    private BreakdownResponse doReportBreakdown(
            OrderVehicleAllocation allocation, BreakdownRequest request, Long tenantId) {

        if (allocation.getAllocationStatus() != VehicleAllocationStatus.IN_TRANSIT) {
            throw new FerosException(
                    "Breakdown can only be reported for a vehicle that is IN_TRANSIT",
                    HttpStatus.BAD_REQUEST);
        }

        // Check no active breakdown already exists for this allocation
        boolean alreadyExists = breakdownRepository
                .existsByVehicleAllocationIdAndIsActiveTrueAndStatusNotIn(
                        allocation.getId(),
                        List.of(BreakdownStatus.RESOLVED, BreakdownStatus.VEHICLE_REPLACED));
        if (alreadyExists) {
            throw new FerosException(
                    "An active breakdown already exists for this vehicle allocation",
                    HttpStatus.CONFLICT);
        }

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow();

        VehicleBreakdown breakdown = VehicleBreakdown.builder()
                .tenant(tenant)
                .vehicle(allocation.getVehicle())
                .vehicleAllocation(allocation)
                .order(allocation.getOrder())
                .breakdownDate(request.getBreakdownDate())
                .location(request.getLocation())
                .breakdownType(request.getBreakdownType())
                .breakdownDuration(request.getBreakdownDuration())
                .reason(request.getReason())
                .notes(request.getNotes())
                .reportedBy(getCurrentUser())
                .status(BreakdownStatus.REPORTED)
                .build();

        // Mark vehicle as BREAKDOWN
        setVehicleStatus(allocation.getVehicle(), VehicleStatusType.BREAKDOWN);

        return mapToResponse(breakdownRepository.save(breakdown));
    }

    // ── replace vehicle ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public BreakdownResponse replaceVehicle(Long orderId, Long breakdownId, BreakdownReplaceRequest request) {
        Long tenantId = getTenantId();

        Order order = orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = breakdownRepository.findByIdAndTenantIdAndIsActiveTrue(breakdownId, tenantId)
                .orElseThrow(() -> new FerosException("Breakdown record not found", HttpStatus.NOT_FOUND));

        if (!breakdown.getOrder().getId().equals(orderId)) {
            throw new FerosException("Breakdown does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getStatus() == BreakdownStatus.VEHICLE_REPLACED) {
            throw new FerosException("Replacement vehicle already assigned for this breakdown", HttpStatus.CONFLICT);
        }

        if (breakdown.getStatus() == BreakdownStatus.RESOLVED) {
            throw new FerosException("Breakdown is already resolved — no replacement needed", HttpStatus.BAD_REQUEST);
        }

        Vehicle replacementVehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getReplacementVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Replacement vehicle not found", HttpStatus.NOT_FOUND));

        if (replacementVehicle.getCurrentStatus() != null &&
                (replacementVehicle.getCurrentStatus().getStatusType() == VehicleStatusType.BREAKDOWN ||
                 replacementVehicle.getCurrentStatus().getStatusType() == VehicleStatusType.IN_REPAIR)) {
            throw new FerosException(
                    "Cannot use replacement vehicle — it is currently " +
                    replacementVehicle.getCurrentStatus().getName(),
                    HttpStatus.CONFLICT);
        }

        if (replacementVehicle.getCurrentStatus() != null &&
                replacementVehicle.getCurrentStatus().getStatusType() != VehicleStatusType.AVAILABLE) {
            throw new FerosException(
                    "Replacement vehicle is not available — current status: " +
                    replacementVehicle.getCurrentStatus().getName(),
                    HttpStatus.CONFLICT);
        }

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow();
        User currentUser = getCurrentUser();
        OrderVehicleAllocation originalAllocation = breakdown.getVehicleAllocation();

        // Create replacement vehicle allocation
        OrderVehicleAllocation replacementAllocation = OrderVehicleAllocation.builder()
                .tenant(tenant)
                .order(order)
                .vehicle(replacementVehicle)
                .allocatedWeight(originalAllocation.getAllocatedWeight())
                .expectedLoadDate(originalAllocation.getExpectedLoadDate())
                .expectedDeliveryDate(
                        request.getExpectedDeliveryDate() != null
                        ? request.getExpectedDeliveryDate()
                        : originalAllocation.getExpectedDeliveryDate())
                .allocationStatus(VehicleAllocationStatus.IN_TRANSIT)
                .allocatedBy(currentUser)
                .remarks("Replacement for breakdown — " +
                        (request.getNotes() != null ? request.getNotes() : ""))
                .isActive(true)
                .build();

        replacementAllocation = vehicleAllocationRepository.save(replacementAllocation);

        // Move LR to replacement allocation + reset odometer baseline to V2's current reading
        Optional<Lr> lrOpt = lrRepository.findByVehicleAllocationId(originalAllocation.getId());
        if (lrOpt.isPresent()) {
            Lr lr = lrOpt.get();
            lr.setVehicleAllocation(replacementAllocation);
            lrRepository.save(lr);

            // Soft-delete V1's TRIP_START so end-trip validation uses V2's ODR
            vehicleMeterReadingRepository
                    .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                            lr.getId(), MeterReadingType.TRIP_START)
                    .ifPresent(r -> {
                        r.setIsActive(false);
                        vehicleMeterReadingRepository.save(r);
                    });

            // Create new TRIP_START for V2 at its current odometer reading
            if (replacementVehicle.getCurrentOdometerReading() != null) {
                vehicleMeterReadingRepository.save(VehicleMeterReading.builder()
                        .tenant(tenant)
                        .vehicle(replacementVehicle)
                        .readingKm(replacementVehicle.getCurrentOdometerReading())
                        .readingType(MeterReadingType.TRIP_START)
                        .lr(lr)
                        .recordedBy(currentUser)
                        .recordedAt(TimeUtil.nowIst())
                        .isActive(true)
                        .build());
            }
        }

        // Staff handling
        List<OrderStaffAllocation> v1Staff = staffAllocationRepository
                .findByVehicleAllocationIdAndIsActiveTrue(originalAllocation.getId());

        User newDriver = null;
        User newCleaner = null;

        if (request.getSelectedDriverId() != null) {
            // Supervisor chose specific staff → cancel all from V1 + V2, assign selected
            for (OrderStaffAllocation sa : v1Staff) {
                sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                sa.setIsActive(false);
                closeVsa(sa.getUser().getId(), tenantId, currentUser);
            }
            staffAllocationRepository.saveAll(v1Staff);

            // Cancel V2's current active staff if any + close their VSA
            vehicleAllocationRepository.findCurrentActiveAllocationForVehicle(replacementVehicle.getId())
                    .ifPresent(v2Alloc -> {
                        List<OrderStaffAllocation> v2Staff = staffAllocationRepository
                                .findByVehicleAllocationIdAndIsActiveTrue(v2Alloc.getId());
                        for (OrderStaffAllocation sa : v2Staff) {
                            sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                            sa.setIsActive(false);
                            closeVsa(sa.getUser().getId(), tenantId, currentUser);
                        }
                        staffAllocationRepository.saveAll(v2Staff);
                    });

            // Assign selected driver
            newDriver = userRepository.findById(request.getSelectedDriverId())
                    .orElseThrow(() -> new FerosException("Selected driver not found", HttpStatus.NOT_FOUND));
            Role driverRole = roleRepository.findByName(RoleName.DRIVER)
                    .orElseThrow(() -> new FerosException("Driver role not found", HttpStatus.INTERNAL_SERVER_ERROR));
            staffAllocationRepository.save(OrderStaffAllocation.builder()
                    .tenant(tenant).order(order).vehicleAllocation(replacementAllocation)
                    .user(newDriver).role(driverRole)
                    .allocationStatus(StaffAllocationStatus.IN_TRANSIT)
                    .allocatedBy(currentUser).isActive(true).build());
            closeVsa(newDriver.getId(), tenantId, currentUser);
            openVsa(tenant, replacementVehicle, newDriver, currentUser);

            // Assign selected cleaner (optional)
            if (request.getSelectedCleanerId() != null) {
                newCleaner = userRepository.findById(request.getSelectedCleanerId())
                        .orElseThrow(() -> new FerosException("Selected cleaner not found", HttpStatus.NOT_FOUND));
                Role cleanerRole = roleRepository.findByName(RoleName.CLEANER)
                        .orElseThrow(() -> new FerosException("Cleaner role not found", HttpStatus.INTERNAL_SERVER_ERROR));
                staffAllocationRepository.save(OrderStaffAllocation.builder()
                        .tenant(tenant).order(order).vehicleAllocation(replacementAllocation)
                        .user(newCleaner).role(cleanerRole)
                        .allocationStatus(StaffAllocationStatus.IN_TRANSIT)
                        .allocatedBy(currentUser).isActive(true).build());
                closeVsa(newCleaner.getId(), tenantId, currentUser);
                openVsa(tenant, replacementVehicle, newCleaner, currentUser);
            }
        } else {
            // No staff choice → auto-move D1+C1 to replacement vehicle
            for (OrderStaffAllocation sa : v1Staff) {
                sa.setVehicleAllocation(replacementAllocation);
                if (sa.getRole().getName() == RoleName.DRIVER) newDriver = sa.getUser();
                else if (sa.getRole().getName() == RoleName.CLEANER) newCleaner = sa.getUser();
                closeVsa(sa.getUser().getId(), tenantId, currentUser);
                openVsa(tenant, replacementVehicle, sa.getUser(), currentUser);
            }
            staffAllocationRepository.saveAll(v1Staff);
        }

        // Sync LR driver/cleaner
        if (lrOpt.isPresent()) {
            Lr lr = lrOpt.get();
            lr.setDriver(newDriver);
            lr.setCleaner(newCleaner);
            lrRepository.save(lr);
        }

        // Sync denormalized currentDriver/currentCleaner on both vehicles
        Vehicle originalVehicle = originalAllocation.getVehicle();
        originalVehicle.setCurrentDriver(null);
        originalVehicle.setCurrentCleaner(null);
        vehicleRepository.save(originalVehicle);

        replacementVehicle.setCurrentDriver(newDriver);
        replacementVehicle.setCurrentCleaner(newCleaner);

        // Mark original allocation as BREAKDOWN + soft-delete
        originalAllocation.setAllocationStatus(VehicleAllocationStatus.BREAKDOWN);
        originalAllocation.setIsActive(false);
        vehicleAllocationRepository.save(originalAllocation);

        setVehicleStatus(replacementVehicle, VehicleStatusType.ASSIGNED);

        breakdown.setStatus(BreakdownStatus.VEHICLE_REPLACED);
        breakdown.setReplacementVehicleAllocation(replacementAllocation);

        return mapToResponse(breakdownRepository.save(breakdown));
    }

    // ── get replacement vehicle's current staff (for the replace dialog) ──────

    @Override
    public VehicleCurrentStaffResponse getReplacementVehicleStaff(Long vehicleId) {
        Long tenantId = getTenantId();
        LocalDate today = LocalDate.now();

        // Prefer an active allocation (ALLOCATED / LR_CREATED); fall back to most recent
        OrderVehicleAllocation alloc = vehicleAllocationRepository
                .findCurrentActiveAllocationForVehicle(vehicleId)
                .orElseGet(() -> {
                    List<OrderVehicleAllocation> history = vehicleAllocationRepository
                            .findByVehicleIdAndTenantIdOrderByCreatedAtDesc(vehicleId, tenantId);
                    return history.isEmpty() ? null : history.get(0);
                });

        if (alloc == null) {
            return VehicleCurrentStaffResponse.builder().build();
        }

        List<OrderStaffAllocation> staffList = staffAllocationRepository
                .findByVehicleAllocationIdAndIsActiveTrue(alloc.getId());

        VehicleCurrentStaffResponse.StaffMember driverMember = null;
        VehicleCurrentStaffResponse.StaffMember cleanerMember = null;

        for (OrderStaffAllocation sa : staffList) {
            boolean hasAttendance = attendanceRepository
                    .existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                            sa.getUser().getId(), tenantId, today);

            VehicleCurrentStaffResponse.StaffMember member = VehicleCurrentStaffResponse.StaffMember.builder()
                    .id(sa.getUser().getId())
                    .name(sa.getUser().getName())
                    .phone(sa.getUser().getPhone())
                    .hasAttendanceToday(hasAttendance)
                    .build();

            if (sa.getRole().getName() == RoleName.DRIVER) {
                driverMember = member;
            } else if (sa.getRole().getName() == RoleName.CLEANER) {
                cleanerMember = member;
            }
        }

        return VehicleCurrentStaffResponse.builder()
                .driver(driverMember)
                .cleaner(cleanerMember)
                .build();
    }

    // ── resolve breakdown (repaired on road) ─────────────────────────────────

    @Override
    @Transactional
    public BreakdownResponse resolveBreakdown(Long orderId, Long breakdownId) {
        Long tenantId = getTenantId();

        orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = breakdownRepository.findByIdAndTenantIdAndIsActiveTrue(breakdownId, tenantId)
                .orElseThrow(() -> new FerosException("Breakdown record not found", HttpStatus.NOT_FOUND));

        if (!breakdown.getOrder().getId().equals(orderId)) {
            throw new FerosException("Breakdown does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getStatus() == BreakdownStatus.VEHICLE_REPLACED) {
            throw new FerosException(
                    "Cannot resolve — replacement vehicle already assigned",
                    HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getStatus() == BreakdownStatus.RESOLVED) {
            throw new FerosException("Breakdown is already resolved", HttpStatus.BAD_REQUEST);
        }

        // Restore vehicle status → ON_TRIP (trip continues)
        setVehicleStatus(breakdown.getVehicle(), VehicleStatusType.ON_TRIP);

        breakdown.setStatus(BreakdownStatus.RESOLVED);
        breakdown.setResolvedAt(TimeUtil.nowIst());

        return mapToResponse(breakdownRepository.save(breakdown));
    }

    // ── cancel false alarm ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelBreakdown(Long orderId, Long breakdownId) {
        Long tenantId = getTenantId();

        orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = breakdownRepository.findByIdAndTenantIdAndIsActiveTrue(breakdownId, tenantId)
                .orElseThrow(() -> new FerosException("Breakdown record not found", HttpStatus.NOT_FOUND));

        if (!breakdown.getOrder().getId().equals(orderId)) {
            throw new FerosException("Breakdown does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getStatus() != BreakdownStatus.REPORTED) {
            throw new FerosException(
                    "Cannot cancel — breakdown is already " + breakdown.getStatus() +
                    ". Only REPORTED breakdowns (before any action) can be cancelled.",
                    HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getReplacementVehicleAllocation() != null) {
            throw new FerosException(
                    "Cannot cancel — replacement vehicle has already been assigned",
                    HttpStatus.CONFLICT);
        }

        // Restore vehicle status → ON_TRIP
        setVehicleStatus(breakdown.getVehicle(), VehicleStatusType.ON_TRIP);

        // Soft-delete the breakdown record
        breakdown.setIsActive(false);
        breakdownRepository.save(breakdown);
    }

    // ── queries ───────────────────────────────────────────────────────────────

    @Override
    public BreakdownResponse getBreakdownByAllocation(Long orderId, Long allocationId) {
        Long tenantId = getTenantId();

        orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = breakdownRepository
                .findFirstByVehicleAllocationIdAndIsActiveTrueOrderByCreatedAtDesc(allocationId)
                .orElseThrow(() -> new FerosException("No breakdown found for this allocation", HttpStatus.NOT_FOUND));

        return mapToResponse(breakdown);
    }

    @Override
    public List<BreakdownResponse> getBreakdownHistoryByVehicle(Long vehicleId) {
        return breakdownRepository
                .findByVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(vehicleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BreakdownResponse> getAllBreakdowns() {
        Long tenantId = getTenantId();
        return breakdownRepository
                .findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ── standalone breakdown (available vehicle — not on any order) ───────────

    @Override
    @Transactional
    public BreakdownResponse reportStandaloneBreakdown(Long vehicleId, BreakdownRequest request) {
        Long tenantId = getTenantId();

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(vehicleId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        if (vehicle.getCurrentStatus() != null &&
                vehicle.getCurrentStatus().getStatusType() == VehicleStatusType.BREAKDOWN) {
            throw new FerosException("Vehicle is already in BREAKDOWN status", HttpStatus.CONFLICT);
        }

        if (vehicle.getCurrentStatus() != null &&
                (vehicle.getCurrentStatus().getStatusType() == VehicleStatusType.ASSIGNED ||
                 vehicle.getCurrentStatus().getStatusType() == VehicleStatusType.ON_TRIP)) {
            OrderVehicleAllocation allocation = vehicleAllocationRepository
                    .findCurrentActiveAllocationForVehicle(vehicleId)
                    .orElseThrow(() -> new FerosException(
                            "Vehicle is currently " + vehicle.getCurrentStatus().getName() +
                            " but no active allocation was found — please contact admin",
                            HttpStatus.BAD_REQUEST));
            return doReportBreakdown(allocation, request, tenantId);
        }

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow();

        VehicleBreakdown breakdown = VehicleBreakdown.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .breakdownDate(request.getBreakdownDate())
                .location(request.getLocation())
                .breakdownType(request.getBreakdownType())
                .breakdownDuration(request.getBreakdownDuration())
                .reason(request.getReason())
                .notes(request.getNotes())
                .reportedBy(getCurrentUser())
                .status(BreakdownStatus.REPORTED)
                .build();

        setVehicleStatus(vehicle, VehicleStatusType.BREAKDOWN);

        return mapToResponse(breakdownRepository.save(breakdown));
    }

    @Override
    @Transactional
    public BreakdownResponse resolveStandaloneBreakdown(Long vehicleId, Long breakdownId) {
        Long tenantId = getTenantId();

        vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(vehicleId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = breakdownRepository.findByIdAndTenantIdAndIsActiveTrue(breakdownId, tenantId)
                .orElseThrow(() -> new FerosException("Breakdown record not found", HttpStatus.NOT_FOUND));

        if (!breakdown.getVehicle().getId().equals(vehicleId)) {
            throw new FerosException("Breakdown does not belong to this vehicle", HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getOrder() != null) {
            throw new FerosException(
                    "This breakdown is linked to an order — use the order breakdown resolve endpoint",
                    HttpStatus.BAD_REQUEST);
        }

        if (breakdown.getStatus() == BreakdownStatus.RESOLVED) {
            throw new FerosException("Breakdown is already resolved", HttpStatus.BAD_REQUEST);
        }

        setVehicleStatus(breakdown.getVehicle(), VehicleStatusType.AVAILABLE);

        breakdown.setStatus(BreakdownStatus.RESOLVED);
        breakdown.setResolvedAt(TimeUtil.nowIst());

        return mapToResponse(breakdownRepository.save(breakdown));
    }

    // ── VSA history helpers ───────────────────────────────────────────────────

    private void closeVsa(Long userId, Long tenantId, User actor) {
        vehicleStaffAssignmentRepository
                .findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(userId, tenantId)
                .ifPresent(vsa -> {
                    vsa.setAssignedTo(TimeUtil.today());
                    vsa.setUnassignedBy(actor);
                    vsa.setUnassignedAt(LocalDateTime.now());
                    vehicleStaffAssignmentRepository.save(vsa);
                });
    }

    private void openVsa(Tenant tenant, Vehicle vehicle, User user, User actor) {
        vehicleStaffAssignmentRepository.save(VehicleStaffAssignment.builder()
                .tenant(tenant).vehicle(vehicle).user(user)
                .assignedFrom(TimeUtil.today()).assignedBy(actor)
                .build());
    }

    // ── mapper ────────────────────────────────────────────────────────────────

    private BreakdownResponse mapToResponse(VehicleBreakdown b) {
        String replacementReg = null;
        Long replacementAllocId = null;
        if (b.getReplacementVehicleAllocation() != null) {
            replacementAllocId = b.getReplacementVehicleAllocation().getId();
            replacementReg = b.getReplacementVehicleAllocation().getVehicle().getRegistrationNumber();
        }

        return BreakdownResponse.builder()
                .id(b.getId())
                .vehicleId(b.getVehicle().getId())
                .vehicleRegistrationNumber(b.getVehicle().getRegistrationNumber())
                .vehicleAllocationId(b.getVehicleAllocation() != null ? b.getVehicleAllocation().getId() : null)
                .orderId(b.getOrder() != null ? b.getOrder().getId() : null)
                .orderNumber(b.getOrder() != null ? b.getOrder().getOrderNumber() : null)
                .breakdownDate(b.getBreakdownDate())
                .location(b.getLocation())
                .breakdownType(b.getBreakdownType().name())
                .breakdownDuration(b.getBreakdownDuration().name())
                .reason(b.getReason())
                .status(b.getStatus().name())
                .replacementVehicleAllocationId(replacementAllocId)
                .replacementVehicleRegistrationNumber(replacementReg)
                .resolvedAt(b.getResolvedAt())
                .reportedById(b.getReportedBy().getId())
                .reportedByName(b.getReportedBy().getName())
                .notes(b.getNotes())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
