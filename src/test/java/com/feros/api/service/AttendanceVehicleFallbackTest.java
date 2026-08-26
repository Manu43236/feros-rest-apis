package com.feros.api.service;

import com.feros.api.config.UserPrincipal;
import com.feros.api.dto.response.AttendanceResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.AttendanceType;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.*;
import com.feros.api.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceVehicleFallbackTest {

    @Mock AttendanceRepository attendanceRepository;
    @Mock TripProofRepository tripProofRepository;
    @Mock TenantRepository tenantRepository;
    @Mock UserRepository userRepository;
    @Mock LrRepository lrRepository;
    @Mock AttendanceTypeRepository attendanceTypeRepository;
    @Mock LeaveTypeRepository leaveTypeRepository;
    @Mock S3Service s3Service;
    @Mock NotificationService notificationService;
    @Mock VehicleRepository vehicleRepository;
    @Mock VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
    @Mock OrderStaffAllocationRepository orderStaffAllocationRepository;
    @Mock LocationResolverService locationResolverService;
    @Mock StaffProfileRepository staffProfileRepository;

    private AttendanceServiceImpl service;

    private static final Long TENANT_ID  = 1L;
    private static final Long ADMIN_ID   = 99L;
    private static final Long RAVADA_ID  = 225L;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(
            attendanceRepository, tripProofRepository, tenantRepository,
            userRepository, lrRepository, attendanceTypeRepository,
            leaveTypeRepository, s3Service, notificationService,
            vehicleRepository, vehicleStaffAssignmentRepository,
            orderStaffAllocationRepository, locationResolverService, staffProfileRepository
        );

        UserPrincipal principal = new UserPrincipal(ADMIN_ID, TENANT_ID, "9999999990", "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("vehicle from order allocation shown when standing assignment has lapsed — RAVADA case")
    void vehicle_standingAssignmentExpired_fallsBackToOrderAllocation() {
        LocalDate date = LocalDate.of(2026, 8, 2);

        // RAVADA — DRIVER, no valid VSA on 2026-08-02 (assigned_to was 2026-07-12)
        Role driverRole = new Role();
        driverRole.setName(RoleName.DRIVER);
        User ravada = new User();
        ravada.setId(RAVADA_ID);
        ravada.setName("RAVADA DEMUDU NAIDU");
        ravada.setPhone("9000000225");
        ravada.setRoles(Set.of(driverRole));

        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);

        AttendanceType presentType = new AttendanceType();
        presentType.setId(1L);
        presentType.setName("Present");

        Attendance att = Attendance.builder()
            .id(500L)
            .user(ravada)
            .tenant(tenant)
            .attendanceDate(date)
            .attendanceType(presentType)
            .markedBy(ravada)
            .approvalStatus(AttendanceApprovalStatus.APPROVED)
            .build();

        // No VSAs overlap on 2026-08-02 (both for latestForVehicle map and per-user lookup)
        when(vehicleStaffAssignmentRepository.findOverlappingForTenant(TENANT_ID, date, date))
            .thenReturn(List.of());
        when(attendanceRepository.findByTenantIdAndAttendanceDateAndIsActiveTrue(TENANT_ID, date))
            .thenReturn(List.of(att));
        when(vehicleStaffAssignmentRepository.findOverlappingByUser(RAVADA_ID, TENANT_ID, date, date))
            .thenReturn(List.of());

        // Active order allocation: vehicle AP39UM8502 covering 2026-08-01 to 2026-08-04
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("AP39UM8502");
        OrderVehicleAllocation ova = new OrderVehicleAllocation();
        ova.setVehicle(vehicle);
        OrderStaffAllocation osa = OrderStaffAllocation.builder()
            .actualStartDate(LocalDate.of(2026, 8, 1))
            .actualEndDate(LocalDate.of(2026, 8, 4))
            .vehicleAllocation(ova)
            .isActive(true)
            .build();
        when(orderStaffAllocationRepository.findActiveOnDateForUser(RAVADA_ID, TENANT_ID, date))
            .thenReturn(List.of(osa));

        List<AttendanceResponse> responses = service.getAttendanceByDate(date);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAssignedVehicleNumber()).isEqualTo("AP39UM8502");
        verify(orderStaffAllocationRepository).findActiveOnDateForUser(RAVADA_ID, TENANT_ID, date);
    }

    @Test
    @DisplayName("vehicle from standing assignment shown when VSA is valid — normal case unaffected")
    void vehicle_validStandingAssignment_noFallbackNeeded() {
        LocalDate date = LocalDate.of(2026, 8, 2);

        Role driverRole = new Role();
        driverRole.setName(RoleName.DRIVER);
        User driver = new User();
        driver.setId(300L);
        driver.setName("Test Driver");
        driver.setPhone("9000000300");
        driver.setRoles(Set.of(driverRole));

        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);

        AttendanceType presentType = new AttendanceType();
        presentType.setId(1L);
        presentType.setName("Present");

        Attendance att = Attendance.builder()
            .id(501L)
            .user(driver)
            .tenant(tenant)
            .attendanceDate(date)
            .attendanceType(presentType)
            .markedBy(driver)
            .approvalStatus(AttendanceApprovalStatus.APPROVED)
            .build();

        Vehicle vehicle = new Vehicle();
        vehicle.setId(10L);
        vehicle.setRegistrationNumber("AP39TZ1234");

        VehicleStaffAssignment vsa = new VehicleStaffAssignment();
        vsa.setUser(driver);
        vsa.setVehicle(vehicle);
        vsa.setAssignedFrom(LocalDate.of(2026, 7, 1));

        // VSA is active — latestForVehicle map includes this driver
        when(vehicleStaffAssignmentRepository.findOverlappingForTenant(TENANT_ID, date, date))
            .thenReturn(List.of(vsa));
        when(attendanceRepository.findByTenantIdAndAttendanceDateAndIsActiveTrue(TENANT_ID, date))
            .thenReturn(List.of(att));
        when(vehicleStaffAssignmentRepository.findOverlappingByUser(300L, TENANT_ID, date, date))
            .thenReturn(List.of(vsa));

        List<AttendanceResponse> responses = service.getAttendanceByDate(date);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAssignedVehicleNumber()).isEqualTo("AP39TZ1234");
        // fallback repo must NOT be called when VSA is valid
        verify(orderStaffAllocationRepository, never()).findActiveOnDateForUser(any(), any(), any());
    }
}
