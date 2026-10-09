package com.feros.api.service;

import com.feros.api.entity.*;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.LeaseCleanerAssignmentLogRepository;
import com.feros.api.repository.LeaseDriverAssignmentLogRepository;
import com.feros.api.repository.OrderStaffAllocationRepository;
import com.feros.api.repository.VehicleStaffAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StaffVehicleDayResolver — payslip == attendance resolution")
class StaffVehicleDayResolverTest {

    @Mock VehicleStaffAssignmentRepository vsaRepository;
    @Mock LeaseDriverAssignmentLogRepository leaseLogRepository;
    @Mock LeaseCleanerAssignmentLogRepository leaseCleanerLogRepository;
    @Mock OrderStaffAllocationRepository orderStaffAllocationRepository;

    StaffVehicleDayResolver resolver;

    static final Long T = 1L;
    static final Long UID = 134L;
    static final LocalDate DATE = LocalDate.of(2026, 9, 14);

    @BeforeEach
    void setUp() {
        resolver = new StaffVehicleDayResolver(vsaRepository, leaseLogRepository, leaseCleanerLogRepository, orderStaffAllocationRepository);
        when(orderStaffAllocationRepository.findActiveOnDateForUser(anyLong(), anyLong(), any()))
                .thenReturn(List.of());
    }

    @Test
    @DisplayName("REGRESSION LOCK (Ramana 14/26): live VSA on A + phantom lease on B → returns A, not the lease")
    void vsaWins_phantomLeaseIgnored() {
        Vehicle wl3553 = vehicle(1L, "AP39WL3553");
        Vehicle ts5428 = vehicle(2L, "AP39TS5428"); // phantom lease vehicle
        VehicleStaffAssignment vsa = vsa(driver(UID), wl3553, DATE.minusDays(5), null);

        var ctx = new StaffVehicleDayResolver.Context(
                Map.of(UID, List.of(vsa)),
                Map.of(UID, ts5428),   // collapsed lease vehicle for Ramana = phantom TS5428
                Map.of(2L, UID),       // TS5428 held by Ramana
                Map.of());

        // VSA covers the day and WL3553 isn't leased (no holder) → VSA wins; the phantom lease is never consulted.
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isEqualTo(wl3553);
    }

    @Test
    @DisplayName("pure-lease driver (no VSA) → falls back to the lease vehicle")
    void pureLease_returnsLeaseVehicle() {
        Vehicle ts5428 = vehicle(2L, "AP39TS5428");
        var ctx = new StaffVehicleDayResolver.Context(Map.of(), Map.of(UID, ts5428), Map.of(2L, UID), Map.of());
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isEqualTo(ts5428);
    }

    @Test
    @DisplayName("Delta B: no VSA, no lease, but on an active order trip → returns the order vehicle")
    void orderTripFallback() {
        Vehicle ord = vehicle(3L, "AP39UM8502");
        OrderVehicleAllocation ova = new OrderVehicleAllocation();
        ova.setVehicle(ord);
        OrderStaffAllocation osa = OrderStaffAllocation.builder().vehicleAllocation(ova).build();
        when(orderStaffAllocationRepository.findActiveOnDateForUser(UID, T, DATE)).thenReturn(List.of(osa));

        var ctx = new StaffVehicleDayResolver.Context(Map.of(), Map.of(), Map.of(), Map.of());
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isEqualTo(ord);
    }

    @Test
    @DisplayName("swap-suppression: another driver took the vehicle later → original falls through (no vehicle)")
    void swappedOut_fallsThrough() {
        Vehicle a = vehicle(1L, "A");
        VehicleStaffAssignment mine   = vsa(driver(UID), a, DATE.minusDays(5), null);
        VehicleStaffAssignment theirs = vsa(driver(200L), a, DATE.minusDays(1), null); // later assignedFrom

        var ctx = new StaffVehicleDayResolver.Context(
                Map.of(UID, List.of(mine), 200L, List.of(theirs)), Map.of(), Map.of(), Map.of());
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isNull();
    }

    @Test
    @DisplayName("lease-displacement: DRIVER's VSA on a vehicle held by another lease driver → falls through")
    void leaseDisplaced_fallsThrough() {
        Vehicle a = vehicle(1L, "A");
        VehicleStaffAssignment mine = vsa(driver(UID), a, DATE.minusDays(5), null);
        var ctx = new StaffVehicleDayResolver.Context(
                Map.of(UID, List.of(mine)), Map.of(), Map.of(1L, 999L), Map.of()); // A held by user 999
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isNull();
    }

    @Test
    @DisplayName("nothing covers the day → null (no vehicle allowance)")
    void nothing_returnsNull() {
        var ctx = new StaffVehicleDayResolver.Context(Map.of(), Map.of(), Map.of(), Map.of());
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isNull();
    }

    @Test
    @DisplayName("pure-lease CLEANER (no VSA) → falls back to the lease vehicle (4th Context map)")
    void pureLeaseCleaner_returnsLeaseVehicle() {
        Vehicle ts5428 = vehicle(2L, "AP39TS5428");
        // cleaner's lease vehicle is merged into leaseVehicleByUser; cleaner holder map drives displacement
        var ctx = new StaffVehicleDayResolver.Context(Map.of(), Map.of(UID, ts5428), Map.of(), Map.of(2L, UID));
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isEqualTo(ts5428);
    }

    @Test
    @DisplayName("lease-displacement CLEANER: VSA on a vehicle held by another lease cleaner → falls through")
    void leaseDisplacedCleaner_fallsThrough() {
        Vehicle a = vehicle(1L, "A");
        VehicleStaffAssignment mine = vsa(cleaner(UID), a, DATE.minusDays(5), null);
        // A held (via lease cleaner log) by user 999 → cleaner holder map, not the driver one
        var ctx = new StaffVehicleDayResolver.Context(
                Map.of(UID, List.of(mine)), Map.of(), Map.of(), Map.of(1L, 999L));
        assertThat(resolver.resolve(ctx, UID, T, DATE)).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static User cleaner(Long id) {
        Role r = new Role();
        r.setName(RoleName.CLEANER);
        User u = new User();
        u.setId(id);
        u.setRoles(Set.of(r));
        return u;
    }

    private static User driver(Long id) {
        Role r = new Role();
        r.setName(RoleName.DRIVER);
        User u = new User();
        u.setId(id);
        u.setRoles(Set.of(r));
        return u;
    }

    private static Vehicle vehicle(Long id, String reg) {
        Vehicle v = new Vehicle();
        v.setId(id);
        v.setRegistrationNumber(reg);
        return v;
    }

    private static VehicleStaffAssignment vsa(User user, Vehicle v, LocalDate from, LocalDate to) {
        VehicleStaffAssignment a = new VehicleStaffAssignment();
        a.setUser(user);
        a.setVehicle(v);
        a.setAssignedFrom(from);
        a.setAssignedTo(to);
        return a;
    }
}
