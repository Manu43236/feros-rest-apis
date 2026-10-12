package com.feros.api.service;

import com.feros.api.entity.*;
import com.feros.api.repository.LeaseShiftAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Phase 4 money-core gate: shift pay per (user, date), two-shifts-sum, client = ₹0/none. */
@ExtendWith(MockitoExtension.class)
class LeaseShiftPayResolverTest {

    @Mock LeaseShiftAssignmentRepository repo;

    private static final Long TENANT = 1L;
    private static final LocalDate D = LocalDate.of(2026, 10, 12);

    private LeaseShiftAssignment row(Long driverUserId, BigDecimal driverPay, Long vehicleId) {
        Vehicle v = new Vehicle(); v.setId(vehicleId); v.setRegistrationNumber("AP01AB" + vehicleId);
        LeaseVehicleAssignment lva = new LeaseVehicleAssignment(); lva.setVehicle(v);
        LeaseShiftAssignment a = new LeaseShiftAssignment();
        a.setLeaseVehicleAssignment(lva);
        a.setWorkDate(D);
        if (driverUserId != null) {
            User u = new User(); u.setId(driverUserId);
            StaffProfile sp = new StaffProfile(); sp.setUser(u);
            a.setDriverStaff(sp);
            a.setDriverPaySnapshot(driverPay);
        }
        return a;
    }

    @Test
    @DisplayName("driver on one shift → that shift's snapshot pay; isShiftDay true")
    void singleShiftPay() {
        when(repo.findOverlappingByTenantId(TENANT, D, D))
                .thenReturn(List.of(row(50L, new BigDecimal("750"), 7L)));
        LeaseShiftPayResolver r = new LeaseShiftPayResolver(repo);
        var ctx = r.buildContext(TENANT, D, D);

        assertThat(r.isShiftDay(ctx, 50L, D)).isTrue();
        assertThat(r.shiftPay(ctx, 50L, D)).isEqualByComparingTo("750");
        assertThat(r.vehicle(ctx, 50L, D).getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("same driver on two shifts the same day → both pays summed (750 + 800 = 1550)")
    void twoShiftsSameDay_sum() {
        when(repo.findOverlappingByTenantId(TENANT, D, D)).thenReturn(List.of(
                row(50L, new BigDecimal("750"), 7L),
                row(50L, new BigDecimal("800"), 7L)));
        LeaseShiftPayResolver r = new LeaseShiftPayResolver(repo);
        var ctx = r.buildContext(TENANT, D, D);

        assertThat(r.shiftPay(ctx, 50L, D)).isEqualByComparingTo("1550");
    }

    @Test
    @DisplayName("client driver (null staff) contributes no pay and no user entry")
    void clientDriver_noPay() {
        when(repo.findOverlappingByTenantId(TENANT, D, D))
                .thenReturn(List.of(row(null, null, 7L)));
        LeaseShiftPayResolver r = new LeaseShiftPayResolver(repo);
        var ctx = r.buildContext(TENANT, D, D);

        assertThat(r.isShiftDay(ctx, 50L, D)).isFalse();
        assertThat(r.shiftPay(ctx, 50L, D)).isEqualByComparingTo("0");
    }
}
