package com.feros.api.service;

import com.feros.api.entity.User;
import com.feros.api.entity.Vehicle;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.LeaseCleanerAssignmentLogRepository;
import com.feros.api.repository.LeaseDriverAssignmentLogRepository;
import com.feros.api.repository.LeaseVehicleAssignmentRepository;
import com.feros.api.repository.LeaseVehicleSessionRepository;
import com.feros.api.repository.OrderStaffAllocationRepository;
import com.feros.api.repository.StaffProfileRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.repository.VehicleStaffAssignmentRepository;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Enforces the FEROS one-vehicle rule across all three assignment systems:
 * normal ({@code VehicleStaffAssignment}), order ({@code OrderStaffAllocation}) and lease
 * ({@code LeaseDriverAssignmentLog}). A driver/cleaner may be on only ONE vehicle at a time.
 *
 * <p>Each assign flow already enforces the rule <em>within</em> its own system; this guard supplies
 * only the MISSING cross-system piece so no existing flow behavior changes:
 * <ul>
 *   <li>normal/order flows → add {@link #assertNotOnLease} + {@link #assertNotInProgress}</li>
 *   <li>lease flow → add {@link #assertNotOnNormalOrOrder} (non-swap) + {@link #assertNotInProgress},
 *       plus {@link #releaseNormalAndOrder} for the "Swap to this lease" action</li>
 *   <li>Vehicles screen → {@link #assertVehicleNotLeased}</li>
 * </ul>
 *
 * <p>Error codes carried on the 409 so the UI can branch:
 * {@code SWAPPABLE_CONFLICT} → offer Swap; {@code HARD_BLOCK} → message only.
 */
@Component
@RequiredArgsConstructor
public class StaffAssignmentGuard {

    public static final String CODE_SWAPPABLE = "SWAPPABLE_CONFLICT";
    public static final String CODE_HARD_BLOCK = "HARD_BLOCK";

    private static final List<StaffAllocationStatus> ACTIVE_ORDER_STATUSES =
            List.of(StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);

    private final VehicleStaffAssignmentRepository vsaRepository;
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;
    private final LeaseDriverAssignmentLogRepository leaseLogRepository;
    private final LeaseCleanerAssignmentLogRepository leaseCleanerLogRepository;
    private final LeaseVehicleAssignmentRepository leaseAssignmentRepository;
    private final LeaseVehicleSessionRepository leaseSessionRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final VehicleRepository vehicleRepository;

    // ── Bulk "busy" marker (one pass, no N+1) ──────────────────────────────────────────────

    /** userId → the vehicle they're currently on (normal/order/lease), for picker "busy" markers.
     *  Precedence lease > order > normal. Absent = free. */
    public java.util.Map<Long, String> currentVehicleByUser(Long tenantId) {
        java.time.LocalDate today = TimeUtil.today();
        java.util.Map<Long, String> map = new java.util.HashMap<>();
        vsaRepository.findOverlappingForTenant(tenantId, today, today)
                .forEach(a -> map.put(a.getUser().getId(), a.getVehicle().getRegistrationNumber()));
        orderStaffAllocationRepository.findActiveInPeriodForTenant(tenantId, today, today)
                .forEach(sa -> map.put(sa.getUser().getId(),
                        sa.getVehicleAllocation().getVehicle().getRegistrationNumber()));
        leaseLogRepository.findAllActiveByTenantId(tenantId).forEach(l -> {
            if (l.getDriverStaff() != null && l.getDriverStaff().getUser() != null)
                map.put(l.getDriverStaff().getUser().getId(),
                        l.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber() + " · lease");
        });
        leaseCleanerLogRepository.findAllActiveByTenantId(tenantId).forEach(l -> {
            if (l.getCleanerStaff() != null && l.getCleanerStaff().getUser() != null)
                map.put(l.getCleanerStaff().getUser().getId(),
                        l.getLeaseVehicleAssignment().getVehicle().getRegistrationNumber() + " · lease");
        });
        return map;
    }

    // ── Detection ─────────────────────────────────────────────────────────────────────────

    /** If the staff member is an active lease driver, a message naming that leased vehicle. */
    public Optional<String> activeLeaseVehicle(Long userId, Long tenantId) {
        return staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(userId, tenantId)
                .flatMap(sp -> leaseLogRepository.findActiveByDriverStaffId(sp.getId(), tenantId)
                        .map(l -> l.getLeaseVehicleAssignment())
                        .or(() -> leaseCleanerLogRepository.findActiveByCleanerStaffId(sp.getId(), tenantId)
                                .map(l -> l.getLeaseVehicleAssignment())))
                .map(a -> a.getVehicle().getRegistrationNumber()
                        + " (lease " + a.getLease().getLeaseNumber() + ")");
    }

    /** If on an open normal assignment or active order trip on a DIFFERENT vehicle, a message naming it. */
    public Optional<String> activeNormalOrOrderVehicle(Long userId, Long tenantId, Long excludeVehicleId) {
        Optional<String> vsa = vsaRepository
                .findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(userId, tenantId)
                .filter(a -> !a.getVehicle().getId().equals(excludeVehicleId))
                .map(a -> a.getVehicle().getRegistrationNumber());
        if (vsa.isPresent()) return vsa;

        return orderStaffAllocationRepository.findActiveAllocationsForUser(userId, ACTIVE_ORDER_STATUSES)
                .stream()
                .filter(sa -> !sa.getVehicleAllocation().getVehicle().getId().equals(excludeVehicleId))
                .findFirst()
                .map(sa -> sa.getVehicleAllocation().getVehicle().getRegistrationNumber()
                        + " (order " + sa.getOrder().getOrderNumber() + ")");
    }

    /** If the staff member has work IN PROGRESS — an order with an LR created / in transit, or an
     *  active lease session — a message describing it. Such work can never be swapped away. */
    public Optional<String> inProgressWork(Long userId, Long tenantId) {
        Optional<String> order = orderStaffAllocationRepository
                .findActiveAllocationsForUser(userId, ACTIVE_ORDER_STATUSES).stream()
                .filter(sa -> {
                    VehicleAllocationStatus vs = sa.getVehicleAllocation().getAllocationStatus();
                    return vs == VehicleAllocationStatus.LR_CREATED || vs == VehicleAllocationStatus.IN_TRANSIT;
                })
                .findFirst()
                .map(sa -> "an active trip for order " + sa.getOrder().getOrderNumber()
                        + " (vehicle " + sa.getVehicleAllocation().getVehicle().getRegistrationNumber() + ")");
        if (order.isPresent()) return order;

        return staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(userId, tenantId)
                .flatMap(sp -> leaseLogRepository.findActiveByDriverStaffId(sp.getId(), tenantId)
                        .map(l -> l.getLeaseVehicleAssignment())
                        .or(() -> leaseCleanerLogRepository.findActiveByCleanerStaffId(sp.getId(), tenantId)
                                .map(l -> l.getLeaseVehicleAssignment())))
                .filter(a -> leaseSessionRepository.findByAssignmentIdAndIsActiveTrue(a.getId()).isPresent())
                .map(a -> "an active lease session on " + a.getVehicle().getRegistrationNumber());
    }

    // ── Asserts (throw 409 with a branch code) ─────────────────────────────────────────────

    /** HARD_BLOCK if the staff member has work in progress (started trip / active session). */
    public void assertNotInProgress(Long userId, Long tenantId, String staffName) {
        inProgressWork(userId, tenantId).ifPresent(where -> {
            throw new FerosException(
                    staffName + " has " + where + ". Complete or end it before reassigning.",
                    HttpStatus.CONFLICT, CODE_HARD_BLOCK);
        });
    }

    /** HARD_BLOCK if the staff member is already on a lease vehicle (for normal/order assign flows). */
    public void assertNotOnLease(Long userId, Long tenantId, String staffName) {
        activeLeaseVehicle(userId, tenantId).ifPresent(where -> {
            throw new FerosException(
                    staffName + " is already assigned to " + where
                            + ". Unassign them from the lease first.",
                    HttpStatus.CONFLICT, CODE_HARD_BLOCK);
        });
    }

    /** SWAPPABLE_CONFLICT if on a normal/order vehicle (for the lease assign flow, non-swap). */
    public void assertNotOnNormalOrOrder(Long userId, Long tenantId, Long excludeVehicleId, String staffName) {
        activeNormalOrOrderVehicle(userId, tenantId, excludeVehicleId).ifPresent(where -> {
            throw new FerosException(
                    staffName + " is already assigned to " + where + ".",
                    HttpStatus.CONFLICT, CODE_SWAPPABLE);
        });
    }

    /** HARD_BLOCK if the vehicle is on an active lease (Vehicles screen must not assign its driver). */
    public void assertVehicleNotLeased(Long vehicleId) {
        if (leaseAssignmentRepository.existsActiveLeaseForVehicle(vehicleId)) {
            throw new FerosException(
                    "This vehicle is on an active lease. Assign its driver from the Lease screen.",
                    HttpStatus.CONFLICT, CODE_HARD_BLOCK);
        }
    }

    // ── Release (swap path) ─────────────────────────────────────────────────────────────────

    /** Close the staff member's open normal assignment and cancel their active order trips, except on
     *  {@code keepVehicleId}. Only reached after {@link #assertNotInProgress}, so nothing in progress. */
    public void releaseNormalAndOrder(Long userId, Long tenantId, User actor, Long keepVehicleId) {
        vsaRepository.findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(userId, tenantId)
                .filter(a -> !a.getVehicle().getId().equals(keepVehicleId))
                .ifPresent(a -> {
                    a.setAssignedTo(TimeUtil.today());
                    a.setUnassignedBy(actor);
                    a.setUnassignedAt(LocalDateTime.now());
                    vsaRepository.save(a);
                    clearVehiclePointer(a.getVehicle(), userId);
                });

        orderStaffAllocationRepository.findActiveAllocationsForUser(userId, ACTIVE_ORDER_STATUSES).stream()
                .filter(sa -> !sa.getVehicleAllocation().getVehicle().getId().equals(keepVehicleId))
                .forEach(sa -> {
                    sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                    sa.setActualEndDate(TimeUtil.today());
                    orderStaffAllocationRepository.save(sa);
                    clearVehiclePointer(sa.getVehicleAllocation().getVehicle(), userId);
                });
    }

    private void clearVehiclePointer(Vehicle vehicle, Long userId) {
        boolean changed = false;
        if (vehicle.getCurrentDriver() != null && vehicle.getCurrentDriver().getId().equals(userId)) {
            vehicle.setCurrentDriver(null);
            changed = true;
        }
        if (vehicle.getCurrentCleaner() != null && vehicle.getCurrentCleaner().getId().equals(userId)) {
            vehicle.setCurrentCleaner(null);
            changed = true;
        }
        if (changed) vehicleRepository.save(vehicle);
    }
}
