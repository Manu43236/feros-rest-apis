package com.feros.api.util;

import com.feros.api.entity.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Vehicle pay (allowance) for a day. The vehicle itself is now resolved by
 * {@code StaffVehicleDayResolver} (the single source of truth shared with the attendance report),
 * so the old lease-first {@code resolveVehicleForDay} is gone — this class is just the money math.
 */
public final class VehiclePayResolver {

    private VehiclePayResolver() {}

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
