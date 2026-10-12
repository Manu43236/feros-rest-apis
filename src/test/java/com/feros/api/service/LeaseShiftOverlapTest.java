package com.feros.api.service;

import com.feros.api.dto.request.ShiftDefinitionRequest;
import com.feros.api.exception.FerosException;
import com.feros.api.service.impl.VehicleLeaseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase 2 gate: shift-window overlap validation (midnight-crossing allowed). */
class LeaseShiftOverlapTest {

    private static ShiftDefinitionRequest shift(int n, String start, String end) {
        ShiftDefinitionRequest d = new ShiftDefinitionRequest();
        d.setShiftNumber(n);
        d.setStartTime(LocalTime.parse(start));
        d.setEndTime(LocalTime.parse(end));
        d.setDriverPay(BigDecimal.valueOf(750));
        return d;
    }

    @Test
    @DisplayName("day + night shift (09:00–21:00, 21:00–09:00) do not overlap")
    void dayAndNight_noOverlap() {
        assertThatCode(() -> VehicleLeaseServiceImpl.assertNoOverlap(
                List.of(shift(1, "09:00", "21:00"), shift(2, "21:00", "09:00"))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("midnight-crossing shift alone is valid")
    void midnightCrossing_alone_valid() {
        assertThatCode(() -> VehicleLeaseServiceImpl.assertNoOverlap(
                List.of(shift(1, "21:00", "09:00"))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("overlapping day shifts are rejected")
    void overlappingDayShifts_rejected() {
        assertThatThrownBy(() -> VehicleLeaseServiceImpl.assertNoOverlap(
                List.of(shift(1, "09:00", "17:00"), shift(2, "16:00", "20:00"))))
                .isInstanceOf(FerosException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    @DisplayName("a night shift that bleeds into the next day's day shift is rejected")
    void nightBleedingIntoDay_rejected() {
        // 22:00–10:00 wraps and collides with 09:00–18:00 on the [09:00,10:00) tail
        assertThatThrownBy(() -> VehicleLeaseServiceImpl.assertNoOverlap(
                List.of(shift(1, "22:00", "10:00"), shift(2, "09:00", "18:00"))))
                .isInstanceOf(FerosException.class)
                .hasMessageContaining("overlap");
    }
}
