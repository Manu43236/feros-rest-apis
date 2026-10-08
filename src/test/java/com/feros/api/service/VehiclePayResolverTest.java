package com.feros.api.service;

import com.feros.api.entity.Vehicle;
import com.feros.api.util.VehiclePayResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Money math only — the per-day vehicle is now resolved by StaffVehicleDayResolver (see that test). */
class VehiclePayResolverTest {

    private Vehicle vehicle(String reg, BigDecimal extraPay) {
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(reg);
        v.setExtraPayEnabled(true);
        v.setExtraPayPerDay(extraPay);
        return v;
    }

    @Test
    @DisplayName("null vehicle → no vehicle pay")
    void nullVehicle() {
        assertThat(VehiclePayResolver.vehiclePayForDay(null, false, BigDecimal.ONE))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("half-day halves the vehicle pay")
    void halfDayFactor() {
        Vehicle v = vehicle("AP39TS5811", new BigDecimal("13.00"));
        assertThat(VehiclePayResolver.vehiclePayForDay(v, false, new BigDecimal("0.5")))
                .isEqualByComparingTo(new BigDecimal("6.50"));
        assertThat(VehiclePayResolver.vehiclePayForDay(v, false, BigDecimal.ONE))
                .isEqualByComparingTo(new BigDecimal("13.00"));
    }

    @Test
    @DisplayName("cleaner uses cleaner extra-pay, not driver extra-pay")
    void cleanerPayPath() {
        Vehicle v = vehicle("AP39TS5811", new BigDecimal("13.00"));
        v.setCleanerExtraPayPerDay(new BigDecimal("10.00"));
        assertThat(VehiclePayResolver.vehiclePayForDay(v, true, BigDecimal.ONE))
                .isEqualByComparingTo(new BigDecimal("10.00"));
    }
}
