package com.feros.api.service;

import com.feros.api.config.UserPrincipal;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.entity.*;
import com.feros.api.entity.master.AttendanceType;
import com.feros.api.entity.Designation;
import com.feros.api.enums.SalaryType;
import com.feros.api.repository.*;
import com.feros.api.service.impl.PayrollServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private PayrollDeductionRepository payrollDeductionRepository;
    @Mock private SalaryAdvanceRepository salaryAdvanceRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private DeductionTypeRepository deductionTypeRepository;
    @Mock private StaffProfileRepository staffProfileRepository;
    @Mock private VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
    @Mock private TenantHolidayRepository tenantHolidayRepository;
    @Mock private NotificationService notificationService;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private NumberGeneratorService numberGenerator;

    private PayrollServiceImpl payrollService;

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID   = 1L;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollServiceImpl(
            payrollRepository, payrollDeductionRepository, salaryAdvanceRepository,
            tenantRepository, userRepository, attendanceRepository,
            deductionTypeRepository, staffProfileRepository,
            vehicleStaffAssignmentRepository, tenantHolidayRepository,
            notificationService, transactionManager, numberGenerator
        );

        // Set up a valid security context for every test
        UserPrincipal principal = new UserPrincipal(USER_ID, TENANT_ID, "9999999999", "ADMIN");
        var auth = new UsernamePasswordAuthenticationToken(
            principal, null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── DAILY salary ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Daily salary: basicPay = dailyRate × (present + halfDay×0.5); leave days are unpaid")
    void generatePayroll_dailySalary_calculatesBasicPayCorrectly() {
        stubCommonDeps(SalaryType.DAILY, null, new BigDecimal("500.00"));
        stubAttendance(20, 2, 1, 0);   // present=20, half=2, leave=1, absent=0

        GeneratePayrollRequest req = buildRequest(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), null, null);
        payrollService.generatePayroll(req);

        Payroll saved = captureFirstSave();
        // effectiveDays = 20 + 2×0.5 = 21 (leave not paid for daily workers)
        // basicPay = 500 × 21 = 10500
        assertThat(saved.getBasicPay()).isEqualByComparingTo("10500.00");
        assertThat(saved.getOvertimePay()).isEqualByComparingTo("0.00");
        assertThat(saved.getGrossPay()).isEqualByComparingTo("10500.00");
    }

    @Test
    @DisplayName("Daily salary with overtime: overtimePay = (dailyRate/8) × 1.5 × hours")
    void generatePayroll_dailySalaryWithOvertime_calculatesOvertimeCorrectly() {
        stubCommonDeps(SalaryType.DAILY, null, new BigDecimal("500.00"));
        stubAttendance(20, 0, 0, 0);

        // overtimeHours = 4
        GeneratePayrollRequest req = buildRequest(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30),
            new BigDecimal("4"), null);
        payrollService.generatePayroll(req);

        Payroll saved = captureFirstSave();
        // basicPay = 500×20 = 10000
        // hourlyRate = 500/8 = 62.5  →  overtimePay = 62.5×1.5×4 = 375
        assertThat(saved.getBasicPay()).isEqualByComparingTo("10000.00");
        assertThat(saved.getOvertimePay()).isEqualByComparingTo("375.00");
        assertThat(saved.getGrossPay()).isEqualByComparingTo("10375.00");
    }

    // ─── MONTHLY salary ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Monthly salary: LOP deducted for absent and half days")
    void generatePayroll_monthlySalary_deductsLopForAbsences() {
        stubCommonDeps(SalaryType.MONTHLY, new BigDecimal("30000.00"), null);
        stubAttendance(25, 0, 2, 2);   // present=25, half=0, leave=2, absent=2

        // Jan 2024: 4 Sundays (7,14,21,28) → workingDays = 27
        GeneratePayrollRequest req = buildRequest(
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), null, null);
        payrollService.generatePayroll(req);

        Payroll saved = captureFirstSave();
        // workingDays=27, lopDays=2 (absent) + 0 (half×0.5) = 2
        // perDayRate = 30000/27 = 1111.1111
        // lopDeduction = 1111.1111×2 = 2222.22 (HALF_UP)
        // basicPay = 30000 - 2222.22 = 27777.78
        assertThat(saved.getBasicPay()).isEqualByComparingTo("27777.78");
    }

    @Test
    @DisplayName("Monthly salary: half days count as 0.5 for LOP")
    void generatePayroll_monthlySalary_halfDayCountsAsHalfForLop() {
        stubCommonDeps(SalaryType.MONTHLY, new BigDecimal("30000.00"), null);
        stubAttendance(24, 2, 1, 0);   // present=24, half=2, leave=1, absent=0

        // Jan 2024 (27 working days), only 2 half days → lopDays = 0 + 2×0.5 = 1
        GeneratePayrollRequest req = buildRequest(
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), null, null);
        payrollService.generatePayroll(req);

        Payroll saved = captureFirstSave();
        // lopDays = 1  →  lopDeduction = 30000/27 × 1 = 1111.11
        // basicPay = 30000 - 1111.11 = 28888.89
        assertThat(saved.getBasicPay()).isEqualByComparingTo("28888.89");
    }

    // ─── Trip bonus ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Trip bonus is added to grossPay")
    void generatePayroll_withTripBonus_addsToGrossPay() {
        stubCommonDeps(SalaryType.DAILY, null, new BigDecimal("500.00"));
        stubAttendance(20, 0, 0, 0);

        GeneratePayrollRequest req = buildRequest(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), null, new BigDecimal("1500.00"));
        payrollService.generatePayroll(req);

        Payroll saved = captureFirstSave();
        // basicPay=10000 + tripBonus=1500 = 11500
        assertThat(saved.getGrossPay()).isEqualByComparingTo("11500.00");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void stubCommonDeps(SalaryType salaryType, BigDecimal monthlySalary, BigDecimal dailyRate) {
        when(payrollRepository.existsByUserIdAndTenantIdAndPayCycleStartDateAndIsActiveTrue(
            anyLong(), anyLong(), any())).thenReturn(false);

        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        StaffProfile profile = new StaffProfile();
        profile.setSalaryType(salaryType);
        if (monthlySalary != null) profile.setMonthlySalary(monthlySalary);
        if (dailyRate != null) {
            Designation designation = new Designation();
            designation.setPayPerDay(dailyRate);
            profile.setDesignation(designation);
        }
        when(staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(USER_ID, TENANT_ID))
            .thenReturn(Optional.of(profile));

        when(vehicleStaffAssignmentRepository.findOverlappingByUser(
            anyLong(), anyLong(), any(), any())).thenReturn(List.of());

        when(tenantHolidayRepository.findHolidayDatesBetween(anyLong(), any(), any()))
            .thenReturn(Set.of());

        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        when(tenantRepository.findByIdAndIsActiveTrue(TENANT_ID)).thenReturn(Optional.of(tenant));

        when(payrollRepository.save(any(Payroll.class))).thenAnswer(i -> i.getArgument(0));
        lenient().doNothing().when(notificationService).sendToUser(any(), any(), any(), any(), any());
        lenient().doNothing().when(notificationService).sendToTenant(any(), any(), any(), any());
    }

    private void stubAttendance(int present, int half, int leave, int absent) {
        List<Attendance> list = new ArrayList<>();
        list.addAll(buildAttendanceEntries("present", present));
        list.addAll(buildAttendanceEntries("half day", half));
        list.addAll(buildAttendanceEntries("leave", leave));
        list.addAll(buildAttendanceEntries("absent", absent));
        when(attendanceRepository.findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrue(
            anyLong(), anyLong(), any(), any())).thenReturn(list);
    }

    private List<Attendance> buildAttendanceEntries(String typeName, int count) {
        List<Attendance> entries = new ArrayList<>();
        AttendanceType type = new AttendanceType();
        type.setName(typeName);
        for (int i = 0; i < count; i++) {
            Attendance a = new Attendance();
            a.setAttendanceType(type);
            a.setAttendanceDate(LocalDate.of(2024, 1, 1).plusDays(i));
            entries.add(a);
        }
        return entries;
    }

    private GeneratePayrollRequest buildRequest(LocalDate start, LocalDate end,
                                                BigDecimal overtimeHours, BigDecimal tripBonus) {
        GeneratePayrollRequest req = new GeneratePayrollRequest();
        req.setUserId(USER_ID);
        req.setPayCycleStartDate(start);
        req.setPayCycleEndDate(end);
        req.setOvertimeHours(overtimeHours);
        req.setTripBonus(tripBonus);
        return req;
    }

    private Payroll captureFirstSave() {
        ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().get(0);
    }
}
