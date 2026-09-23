package com.feros.api.service;

import com.feros.api.config.UserPrincipal;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.entity.*;
import com.feros.api.entity.master.AttendanceType;
import com.feros.api.entity.Designation;
import com.feros.api.enums.PayrollStatus;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    // ─── DAILY salary (unchanged behaviour) ───────────────────────────────────

    @Test
    @DisplayName("Daily salary: basicPay = dailyRate × (present + halfDay×0.5); leave days are unpaid")
    void generatePayroll_dailySalary_calculatesBasicPayCorrectly() {
        stubCommonDeps(SalaryType.DAILY, null, new BigDecimal("500.00"), null, false);
        stubAttendance(20, 2, 1, 0);   // present=20, half=2, leave=1, absent=0

        payrollService.generatePayroll(buildRequest(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), null, null));

        Payroll saved = captureFirstSave();
        // effectiveDays = 20 + 2×0.5 = 21 → basicPay = 500 × 21 = 10500
        assertThat(saved.getBasicPay()).isEqualByComparingTo("10500.00");
        assertThat(saved.getGrossPay()).isEqualByComparingTo("10500.00");
    }

    // ─── MONTHLY salary: off-days model ────────────────────────────────────────

    @Test
    @DisplayName("Monthly + skipCalendar: offs beyond allowance deducted at monthly/daysInMonth")
    void generatePayroll_monthly_offsBeyondAllowance_deducts() {
        // Aug 2026: 31 days, skipCalendar ON → expected 31. present 27 → offs 4.
        stubCommonDeps(SalaryType.MONTHLY, new BigDecimal("26500.00"), null, 3, true);
        stubAttendance(27, 0, 0, 4);

        payrollService.generatePayroll(buildRequest(
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, null));

        Payroll saved = captureFirstSave();
        // offs 4, allowed 3 → unpaid 1 → 26500/31 = 854.84 → basic 25645.16
        assertThat(saved.getBasicPay()).isEqualByComparingTo("25645.16");
        assertThat(saved.getAllowedOffDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("Monthly: offs within allowance → full salary")
    void generatePayroll_monthly_offsWithinAllowance_fullSalary() {
        stubCommonDeps(SalaryType.MONTHLY, new BigDecimal("26500.00"), null, 4, true);
        stubAttendance(27, 0, 0, 4);   // offs 4, allowed 4 → no cut

        payrollService.generatePayroll(buildRequest(
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, null));

        Payroll saved = captureFirstSave();
        assertThat(saved.getBasicPay()).isEqualByComparingTo("26500.00");
    }

    @Test
    @DisplayName("Monthly: leave counts as an off, half-day counts as 0.5")
    void generatePayroll_monthly_leaveAndHalfCounted() {
        // skipCalendar ON, expected 31. present 25, half 2, leave 2, absent 2.
        stubCommonDeps(SalaryType.MONTHLY, new BigDecimal("31000.00"), null, 3, true);
        stubAttendance(25, 2, 2, 2);

        payrollService.generatePayroll(buildRequest(
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, null));

        Payroll saved = captureFirstSave();
        // effectivePresent = 25 + 1 = 26 → offs = 31 - 26 = 5 → unpaid = 5 - 3 = 2
        // perDay = 31000/31 = 1000 → cut 2000 → basic 29000
        assertThat(saved.getBasicPay()).isEqualByComparingTo("29000.00");
    }

    // ─── Trip bonus ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Trip bonus is added to grossPay")
    void generatePayroll_withTripBonus_addsToGrossPay() {
        stubCommonDeps(SalaryType.DAILY, null, new BigDecimal("500.00"), null, false);
        stubAttendance(20, 0, 0, 0);

        payrollService.generatePayroll(buildRequest(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), null, new BigDecimal("1500.00")));

        Payroll saved = captureFirstSave();
        // basicPay=10000 + tripBonus=1500 = 11500
        assertThat(saved.getGrossPay()).isEqualByComparingTo("11500.00");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void stubCommonDeps(SalaryType salaryType, BigDecimal monthlySalary,
                                BigDecimal dailyRate, Integer allowedOffDays, boolean skipCalendar) {
        when(payrollRepository.findOverlappingPayroll(anyLong(), anyLong(), any(), any(), any()))
            .thenReturn(Optional.empty());
        lenient().when(tenantHolidayRepository.findHolidayDatesBetween(anyLong(), any(), any()))
            .thenReturn(java.util.Set.of());

        User user = new User();
        user.setId(USER_ID);
        user.setRoles(new HashSet<>());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        StaffProfile profile = new StaffProfile();
        profile.setSalaryType(salaryType);
        if (monthlySalary != null) profile.setMonthlySalary(monthlySalary);
        profile.setAllowedOffDays(allowedOffDays);
        profile.setSkipCalendar(skipCalendar);
        if (dailyRate != null) {
            Designation designation = new Designation();
            designation.setPayPerDay(dailyRate);
            profile.setDesignation(designation);
        }
        when(staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(USER_ID, TENANT_ID))
            .thenReturn(Optional.of(profile));

        when(vehicleStaffAssignmentRepository.findOverlappingByUser(
            anyLong(), anyLong(), any(), any())).thenReturn(List.of());

        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        when(tenantRepository.findByIdAndIsActiveTrue(TENANT_ID)).thenReturn(Optional.of(tenant));

        when(payrollRepository.save(any(Payroll.class))).thenAnswer(i -> i.getArgument(0));
        lenient().doNothing().when(notificationService).sendToUser(any(), any(), any(), any(), any());
    }

    private void stubAttendance(int present, int half, int leave, int absent) {
        List<Attendance> list = new ArrayList<>();
        list.addAll(buildAttendanceEntries("present", present));
        list.addAll(buildAttendanceEntries("half day", half));
        list.addAll(buildAttendanceEntries("leave", leave));
        list.addAll(buildAttendanceEntries("absent", absent));
        when(attendanceRepository
            .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrueAndApprovalStatus(
                anyLong(), anyLong(), any(), any(), any())).thenReturn(list);
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
