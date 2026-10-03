package com.feros.api.util;

import com.feros.api.entity.LeaseDriverAssignmentLog;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleStaffAssignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the vehicle a staff member was on for a given day and the vehicle pay for that day.
 *
 * <p>Driver-on-vehicle lives in TWO parallel systems: {@link VehicleStaffAssignment} (VSA, written by
 * the order flow + vehicle-screen assign) and {@code lease_driver_assignment_logs} (written only by the
 * lease module, which NEVER writes VSA). A leased vehicle's driver exists only in the lease log, so a
 * VSA-only lookup silently drops it — the payroll + payslip-annexure bug this fixes.
 *
 * <p>Resolution mirrors the attendance screen: lease-first (a lease day supersedes any stale VSA),
 * then VSA, else none. A pure-VSA driver has no lease log, so behavior is unchanged for them.
 */
public final class VehiclePayResolver {

    private VehiclePayResolver() {}

    /** The vehicle this driver was on for {@code date}: lease-first, then VSA, else null. */
    public static Vehicle resolveVehicleForDay(LocalDate date,
                                               List<VehicleStaffAssignment> vsas,
                                               List<LeaseDriverAssignmentLog> leaseLogs) {
        Optional<LeaseDriverAssignmentLog> lease = leaseLogs.stream()
                .filter(l -> !date.isBefore(l.getAssignedAt().toLocalDate())
                        && (l.getUnassignedAt() == null || !date.isAfter(l.getUnassignedAt().toLocalDate())))
                .max(Comparator.comparing(LeaseDriverAssignmentLog::getAssignedAt));
        if (lease.isPresent()) {
            return lease.get().getLeaseVehicleAssignment().getVehicle();
        }
        // mirrors attendance display: pick latest VSA by assignedFrom then createdAt
        return vsas.stream()
                .filter(a -> !date.isBefore(a.getAssignedFrom())
                        && (a.getAssignedTo() == null || !date.isAfter(a.getAssignedTo())))
                .max(Comparator.comparing(VehicleStaffAssignment::getAssignedFrom)
                        .thenComparing(VehicleStaffAssignment::getCreatedAt))
                .map(VehicleStaffAssignment::getVehicle)
                .orElse(null);
    }

    /** Vehicle pay for a day: cleaner extra-pay or driver extra-pay, scaled by the day factor (0.5 for half-days). */
    public static BigDecimal vehiclePayForDay(Vehicle vehicle, boolean isCleaner, BigDecimal factor) {
        if (vehicle == null) return BigDecimal.ZERO;
        if (isCleaner) {
            BigDecimal cp = vehicle.getCleanerExtraPayPerDay();
            if (cp != null && cp.compareTo(BigDecimal.ZERO) > 0) {
                return cp.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        }
        if (Boolean.TRUE.equals(vehicle.getExtraPayEnabled()) && vehicle.getExtraPayPerDay() != null) {
            return vehicle.getExtraPayPerDay().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
