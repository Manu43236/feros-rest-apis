package com.feros.api.service;

import com.feros.api.entity.*;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StaffAssignmentGuard — one-vehicle rule across normal/order/lease")
class StaffAssignmentGuardTest {

    @Mock VehicleStaffAssignmentRepository vsaRepository;
    @Mock OrderStaffAllocationRepository orderStaffAllocationRepository;
    @Mock LeaseDriverAssignmentLogRepository leaseLogRepository;
    @Mock LeaseCleanerAssignmentLogRepository leaseCleanerLogRepository;
    @Mock LeaseVehicleAssignmentRepository leaseAssignmentRepository;
    @Mock LeaseVehicleSessionRepository leaseSessionRepository;
    @Mock StaffProfileRepository staffProfileRepository;
    @Mock VehicleRepository vehicleRepository;

    StaffAssignmentGuard guard;

    static final Long T = 1L;
    static final Long UID = 100L;
    static final Long SPID = 200L;

    @BeforeEach
    void setUp() {
        guard = new StaffAssignmentGuard(vsaRepository, orderStaffAllocationRepository, leaseLogRepository,
                leaseCleanerLogRepository, leaseAssignmentRepository, leaseSessionRepository, staffProfileRepository, vehicleRepository);
        // default "free" everywhere unless a test overrides
        when(vsaRepository.findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(orderStaffAllocationRepository.findActiveAllocationsForUser(anyLong(), any()))
                .thenReturn(List.of());
        when(staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(anyLong(), anyLong()))
                .thenReturn(Optional.of(staffProfile()));
        when(leaseLogRepository.findActiveByDriverStaffId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(leaseAssignmentRepository.existsActiveLeaseForVehicle(anyLong())).thenReturn(false);
    }

    // ── on-lease → HARD_BLOCK (normal/order flows) ──────────────────────────────

    @Test
    @DisplayName("driver on a lease → assertNotOnLease throws HARD_BLOCK")
    void onLease_hardBlock() {
        when(leaseLogRepository.findActiveByDriverStaffId(SPID, T))
                .thenReturn(Optional.of(leaseLog("AP39TS5428", "LSE21262788", 9L)));

        assertThatThrownBy(() -> guard.assertNotOnLease(UID, T, "Ramana"))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_HARD_BLOCK));
    }

    @Test
    @DisplayName("free driver → assertNotOnLease passes")
    void free_assertNotOnLease_passes() {
        assertThatCode(() -> guard.assertNotOnLease(UID, T, "Ramana")).doesNotThrowAnyException();
    }

    // ── on normal/order → SWAPPABLE_CONFLICT (lease flow, non-swap) ─────────────

    @Test
    @DisplayName("driver on a normal VSA → assertNotOnNormalOrOrder throws SWAPPABLE_CONFLICT")
    void onNormalVsa_swappable() {
        when(vsaRepository.findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(UID, T))
                .thenReturn(Optional.of(vsa(vehicle(10L, "AP39WL3553"))));

        assertThatThrownBy(() -> guard.assertNotOnNormalOrOrder(UID, T, 99L, "Ramana"))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_SWAPPABLE));
    }

    @Test
    @DisplayName("driver on an order → assertNotOnNormalOrOrder throws SWAPPABLE_CONFLICT")
    void onOrder_swappable() {
        when(orderStaffAllocationRepository.findActiveAllocationsForUser(eq(UID), any()))
                .thenReturn(List.of(orderAlloc(20L, "AP39UM8502", "ORD-1", VehicleAllocationStatus.ALLOCATED)));

        assertThatThrownBy(() -> guard.assertNotOnNormalOrOrder(UID, T, 99L, "Ramana"))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_SWAPPABLE));
    }

    @Test
    @DisplayName("driver already on the SAME target vehicle → assertNotOnNormalOrOrder passes (idempotent)")
    void sameVehicle_passes() {
        when(vsaRepository.findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(UID, T))
                .thenReturn(Optional.of(vsa(vehicle(99L, "AP39WL3553"))));

        assertThatCode(() -> guard.assertNotOnNormalOrOrder(UID, T, 99L, "Ramana")).doesNotThrowAnyException();
    }

    // ── in-progress → HARD_BLOCK (no swap) ──────────────────────────────────────

    @Test
    @DisplayName("order with LR created / in transit → assertNotInProgress throws HARD_BLOCK")
    void orderInProgress_hardBlock() {
        when(orderStaffAllocationRepository.findActiveAllocationsForUser(eq(UID), any()))
                .thenReturn(List.of(orderAlloc(20L, "AP39UM8502", "ORD-1", VehicleAllocationStatus.IN_TRANSIT)));

        assertThatThrownBy(() -> guard.assertNotInProgress(UID, T, "Ramana"))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_HARD_BLOCK));
    }

    @Test
    @DisplayName("active lease session → assertNotInProgress throws HARD_BLOCK")
    void leaseSessionInProgress_hardBlock() {
        LeaseDriverAssignmentLog log = leaseLog("AP39TS5428", "LSE-1", 9L);
        when(leaseLogRepository.findActiveByDriverStaffId(SPID, T)).thenReturn(Optional.of(log));
        when(leaseSessionRepository.findByAssignmentIdAndIsActiveTrue(9L))
                .thenReturn(Optional.of(new LeaseVehicleSession()));

        assertThatThrownBy(() -> guard.assertNotInProgress(UID, T, "Ramana"))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_HARD_BLOCK));
    }

    @Test
    @DisplayName("order ALLOCATED but not started → assertNotInProgress passes (swappable, not blocked)")
    void orderAllocatedNotStarted_passes() {
        when(orderStaffAllocationRepository.findActiveAllocationsForUser(eq(UID), any()))
                .thenReturn(List.of(orderAlloc(20L, "AP39UM8502", "ORD-1", VehicleAllocationStatus.ALLOCATED)));

        assertThatCode(() -> guard.assertNotInProgress(UID, T, "Ramana")).doesNotThrowAnyException();
    }

    // ── leased vehicle (Vehicles screen) ────────────────────────────────────────

    @Test
    @DisplayName("vehicle on an active lease → assertVehicleNotLeased throws HARD_BLOCK")
    void leasedVehicle_hardBlock() {
        when(leaseAssignmentRepository.existsActiveLeaseForVehicle(77L)).thenReturn(true);

        assertThatThrownBy(() -> guard.assertVehicleNotLeased(77L))
                .isInstanceOf(FerosException.class)
                .satisfies(e -> assertThat(((FerosException) e).getCode()).isEqualTo(StaffAssignmentGuard.CODE_HARD_BLOCK));
    }

    // ── swap release ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("releaseNormalAndOrder closes the open VSA + cancels the order alloc + clears vehicle pointer")
    void release_closesVsaAndOrder() {
        User driver = user(UID, "Ramana");
        Vehicle vehA = vehicle(10L, "AP39WL3553");
        vehA.setCurrentDriver(driver);
        VehicleStaffAssignment vsa = vsa(vehA);
        vsa.setUser(driver);
        when(vsaRepository.findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(UID, T))
                .thenReturn(Optional.of(vsa));
        OrderStaffAllocation osa = orderAlloc(20L, "AP39UM8502", "ORD-1", VehicleAllocationStatus.ALLOCATED);
        when(orderStaffAllocationRepository.findActiveAllocationsForUser(eq(UID), any()))
                .thenReturn(List.of(osa));

        guard.releaseNormalAndOrder(UID, T, user(999L, "Admin"), 99L /* keep a different vehicle */);

        assertThat(vsa.getAssignedTo()).isNotNull();
        assertThat(vsa.getUnassignedAt()).isNotNull();
        assertThat(osa.getAllocationStatus()).isEqualTo(StaffAllocationStatus.CANCELLED);
        assertThat(vehA.getCurrentDriver()).isNull();
        verify(vsaRepository).save(vsa);
        verify(orderStaffAllocationRepository).save(osa);
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    private static User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        return u;
    }

    private static Vehicle vehicle(Long id, String reg) {
        Vehicle v = new Vehicle();
        v.setId(id);
        v.setRegistrationNumber(reg);
        return v;
    }

    private static StaffProfile staffProfile() {
        return StaffProfile.builder().id(SPID).user(user(UID, "Ramana")).build();
    }

    private static VehicleStaffAssignment vsa(Vehicle v) {
        VehicleStaffAssignment a = new VehicleStaffAssignment();
        a.setUser(user(UID, "Ramana"));
        a.setVehicle(v);
        return a;
    }

    private static OrderStaffAllocation orderAlloc(Long vehId, String reg, String orderNo, VehicleAllocationStatus status) {
        OrderVehicleAllocation ova = new OrderVehicleAllocation();
        ova.setVehicle(vehicle(vehId, reg));
        ova.setAllocationStatus(status);
        Order order = Order.builder().orderNumber(orderNo).build();
        return OrderStaffAllocation.builder().user(user(UID, "Ramana")).vehicleAllocation(ova).order(order).build();
    }

    private static LeaseDriverAssignmentLog leaseLog(String reg, String leaseNo, Long assignmentId) {
        VehicleLease lease = VehicleLease.builder().leaseNumber(leaseNo).build();
        LeaseVehicleAssignment lva = LeaseVehicleAssignment.builder()
                .id(assignmentId).vehicle(vehicle(50L, reg)).lease(lease).build();
        return LeaseDriverAssignmentLog.builder().leaseVehicleAssignment(lva).build();
    }
}
