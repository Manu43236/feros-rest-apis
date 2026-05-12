package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.BreakdownRequest;
import com.feros.api.dto.request.BreakdownReplaceRequest;
import com.feros.api.dto.response.BreakdownResponse;
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

        // Validate replacement vehicle
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

        OrderVehicleAllocation originalAllocation = breakdown.getVehicleAllocation();

        // Create replacement vehicle allocation (same weight — do NOT touch order.totalWeightFulfilled)
        OrderVehicleAllocation replacementAllocation = OrderVehicleAllocation.builder()
                .tenant(tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow())
                .order(order)
                .vehicle(replacementVehicle)
                .allocatedWeight(originalAllocation.getAllocatedWeight())
                .expectedLoadDate(originalAllocation.getExpectedLoadDate())
                .expectedDeliveryDate(
                        request.getExpectedDeliveryDate() != null
                        ? request.getExpectedDeliveryDate()
                        : originalAllocation.getExpectedDeliveryDate())
                .allocationStatus(VehicleAllocationStatus.IN_TRANSIT) // goods already moving
                .allocatedBy(getCurrentUser())
                .remarks("Replacement for breakdown — " +
                        (request.getNotes() != null ? request.getNotes() : ""))
                .isActive(true)
                .build();

        replacementAllocation = vehicleAllocationRepository.save(replacementAllocation);

        // Reassign the LR to replacement allocation (same LR travels with goods)
        Optional<Lr> lrOpt = lrRepository.findByVehicleAllocationId(originalAllocation.getId());
        if (lrOpt.isPresent()) {
            Lr lr = lrOpt.get();
            lr.setVehicleAllocation(replacementAllocation);
            lrRepository.save(lr);
        }

        // Handle staff
        List<OrderStaffAllocation> staffList = staffAllocationRepository
                .findByVehicleAllocationIdAndIsActiveTrue(originalAllocation.getId());

        if (Boolean.TRUE.equals(request.getTransferStaff())) {
            // Transfer driver + cleaner to replacement allocation
            for (OrderStaffAllocation sa : staffList) {
                sa.setVehicleAllocation(replacementAllocation);
            }
            staffAllocationRepository.saveAll(staffList);
        } else {
            // Cancel existing staff — admin will assign fresh
            for (OrderStaffAllocation sa : staffList) {
                sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                sa.setIsActive(false);
            }
            staffAllocationRepository.saveAll(staffList);
        }

        // Mark original allocation as BREAKDOWN + soft-delete
        originalAllocation.setAllocationStatus(VehicleAllocationStatus.BREAKDOWN);
        originalAllocation.setIsActive(false);
        vehicleAllocationRepository.save(originalAllocation);

        // Set replacement vehicle status → ASSIGNED (then ON_TRIP when LR dispatched)
        setVehicleStatus(replacementVehicle, VehicleStatusType.ASSIGNED);

        // Update breakdown record
        breakdown.setStatus(BreakdownStatus.VEHICLE_REPLACED);
        breakdown.setReplacementVehicleAllocation(replacementAllocation);
        breakdown.setResolvedAt(TimeUtil.nowIst());

        return mapToResponse(breakdownRepository.save(breakdown));
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
                .findByVehicleAllocationIdAndIsActiveTrue(allocationId)
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
            throw new FerosException(
                    "Vehicle is currently " + vehicle.getCurrentStatus().getName() +
                    " — report breakdown from the active order instead",
                    HttpStatus.BAD_REQUEST);
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
