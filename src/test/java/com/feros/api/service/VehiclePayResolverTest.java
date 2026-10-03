package com.feros.api.service;

import com.feros.api.entity.LeaseDriverAssignmentLog;
import com.feros.api.entity.LeaseVehicleAssignment;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleStaffAssignment;
import com.feros.api.util.VehiclePayResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePayResolverTest {

    private static final LocalDate DAY = LocalDate.of(2026, 9, 19);

    private Vehicle vehicle(String reg, BigDecimal extraPay) {
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(reg);
        v.setExtraPayEnabled(true);
        v.setExtraPayPerDay(extraPay);
        return v;
    }

    private VehicleStaffAssignment vsa(Vehicle v, LocalDate from, LocalDate to) {
        VehicleStaffAssignment a = new VehicleStaffAssignment();
        a.setVehicle(v);
        a.setAssignedFrom(from);
        a.setAssignedTo(to);
        return a;
    }

    private LeaseDriverAssignmentLog leaseLog(Vehicle v, LocalDateTime assignedAt, LocalDateTime unassignedAt) {
        LeaseVehicleAssignment lva = new LeaseVehicleAssignment();
        lva.setVehicle(v);
        LeaseDriverAssignmentLog l = new LeaseDriverAssignmentLog();
        l.setLeaseVehicleAssignment(lva);
        l.setAssignedAt(assignedAt);
        l.setUnassignedAt(unassignedAt);
        return l;
    }

    @Test
    @DisplayName("lease log wins over a stale VSA for the same day — the core bug")
    void leaseTakesPrecedenceOverVsa() {
        Vehicle leased = vehicle("AP39TS5811", new BigDecimal("13.00"));
        Vehicle stale  = vehicle("AP00XX0000", new BigDecimal("50.00"));

        Vehicle resolved = VehiclePayResolver.resolveVehicleForDay(
                DAY,
                List.of(vsa(stale, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))),
                List.of(leaseLog(leased, LocalDateTime.of(2026, 9, 15, 8, 0), null)));

        assertThat(resolved.getRegistrationNumber()).isEqualTo("AP39TS5811");
    }

    @Test
    @DisplayName("no lease log → falls back to a covering VSA (pure-VSA driver unchanged)")
    void vsaUsedWhenNoLease() {
        Vehicle v = vehicle("AP39TS5811", new BigDecimal("13.00"));
        Vehicle resolved = VehiclePayResolver.resolveVehicleForDay(
                DAY,
                List.of(vsa(v, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))),
                List.of());
        assertThat(resolved.getRegistrationNumber()).isEqualTo("AP39TS5811");
    }

    @Test
    @DisplayName("neither source covers the day → null → no vehicle pay")
    void nothingCoversDay() {
        Vehicle resolved = VehiclePayResolver.resolveVehicleForDay(DAY, List.of(), List.of());
        assertThat(resolved).isNull();
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
