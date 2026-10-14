package com.feros.api.service;

import com.feros.api.dto.request.ShiftAssignmentRequest;
import com.feros.api.entity.*;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.repository.*;
import com.feros.api.service.impl.VehicleLeaseServiceImpl;
import com.feros.api.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Phase 3 gate — decision A: assigning a driver to a shift auto-creates attendance on the shift's
 *  START date (so attendance date == payroll date, even for the night shift). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LeaseShiftAssignmentTest {

    @Mock VehicleLeaseRepository leaseRepository;
    @Mock LeaseVehicleAssignmentRepository assignmentRepository;
    @Mock LeaseShiftRepository leaseShiftRepository;
    @Mock LeaseShiftAssignmentRepository leaseShiftAssignmentRepository;
    @Mock StaffProfileRepository staffProfileRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AttendanceTypeRepository attendanceTypeRepository;
    @Mock UserRepository userRepository;
    @Mock TenantRepository tenantRepository;
    @Mock com.feros.api.service.NotificationService notificationService;
    @Mock com.feros.api.repository.LeaseDriverAssignmentLogRepository leaseDriverLogRepository;
    @Mock com.feros.api.repository.LeaseCleanerAssignmentLogRepository leaseCleanerLogRepository;
    @Mock com.feros.api.service.StaffAssignmentGuard staffAssignmentGuard;

    @InjectMocks VehicleLeaseServiceImpl service;

    @Test
    @DisplayName("assigning a night-shift driver creates PENDING attendance on the shift start date")
    void assignShift_autoCreatesAttendance_onStartDate() {
        LocalDate workDate = LocalDate.of(2026, 10, 12); // night shift 21:00→09:00 credited here

        Tenant tenant = new Tenant(); tenant.setId(1L);
        User currentUser = new User(); currentUser.setId(10L);
        User driverUser = new User(); driverUser.setId(50L); driverUser.setName("D");

        VehicleLease lease = new VehicleLease(); lease.setId(100L); lease.setTenant(tenant); lease.setShiftDuty(true);
        Vehicle vehicle = new Vehicle(); vehicle.setId(7L); vehicle.setRegistrationNumber("AP01AB1234");
        LeaseVehicleAssignment assignment = new LeaseVehicleAssignment();
        assignment.setId(200L); assignment.setLease(lease); assignment.setVehicle(vehicle);

        LeaseShift shift = new LeaseShift();
        shift.setId(300L); shift.setLease(lease); shift.setShiftNumber(2);
        shift.setStartTime(LocalTime.of(21, 0)); shift.setEndTime(LocalTime.of(9, 0));
        shift.setDriverPay(new BigDecimal("750"));

        StaffProfile driver = new StaffProfile(); driver.setId(5L); driver.setUser(driverUser);
        com.feros.api.entity.master.AttendanceType present = new com.feros.api.entity.master.AttendanceType();
        present.setName("PRESENT");

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(10L);

            when(tenantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(tenant));
            when(leaseRepository.findByIdAndTenantIdAndIsActiveTrue(100L, 1L)).thenReturn(Optional.of(lease));
            when(assignmentRepository.findByIdAndLeaseId(200L, 100L)).thenReturn(Optional.of(assignment));
            when(leaseShiftRepository.findById(300L)).thenReturn(Optional.of(shift));
            when(staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(50L, 1L)).thenReturn(Optional.of(driver));
            when(leaseShiftAssignmentRepository.findByDriverStaffIdAndWorkDate(5L, workDate)).thenReturn(List.of());
            when(leaseShiftAssignmentRepository.findByLeaseVehicleAssignmentIdAndLeaseShiftIdAndWorkDate(200L, 300L, workDate))
                    .thenReturn(Optional.empty());
            when(leaseShiftAssignmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(attendanceRepository.findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(50L, 1L, workDate))
                    .thenReturn(Optional.empty());
            when(attendanceTypeRepository.findByNameIgnoreCase("PRESENT")).thenReturn(Optional.of(present));
            when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));

            ShiftAssignmentRequest req = new ShiftAssignmentRequest();
            req.setShiftId(300L); req.setWorkDate(workDate); req.setDriverStaffId(50L); req.setCleanerStaffId(null);

            service.assignShift(100L, 200L, req);

            ArgumentCaptor<Attendance> att = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(att.capture());
            assertThat(att.getValue().getAttendanceDate()).isEqualTo(workDate);
            assertThat(att.getValue().getApprovalStatus()).isEqualTo(AttendanceApprovalStatus.PENDING);
            assertThat(att.getValue().getUser().getId()).isEqualTo(50L);
        }
    }
}
