package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.ApprovePayrollRequest;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.dto.request.SalaryAdvanceRequest;
import com.feros.api.dto.response.PayrollDeductionResponse;
import com.feros.api.dto.response.PayrollResponse;
import com.feros.api.dto.response.SalaryAdvanceResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.DeductionType;
import com.feros.api.enums.PayrollStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.entity.StaffProfile;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.PayrollService;
import com.feros.api.enums.NotificationType;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final SalaryAdvanceRepository salaryAdvanceRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final DeductionTypeRepository deductionTypeRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final NotificationService notificationService;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public SalaryAdvanceResponse createAdvance(SalaryAdvanceRequest request) {
        Long tenantId = getCurrentTenantId();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        SalaryAdvance advance = SalaryAdvance.builder()
                .tenant(getCurrentTenant())
                .user(user)
                .advanceDate(request.getAdvanceDate() != null ? request.getAdvanceDate() : TimeUtil.today())
                .amount(request.getAmount())
                .reason(request.getReason())
                .totalRepaid(BigDecimal.ZERO)
                .balanceAmount(request.getAmount())
                .isFullyRepaid(false)
                .approvedBy(getCurrentUser())
                .approvedAt(TimeUtil.nowIst())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        return mapToAdvanceResponse(salaryAdvanceRepository.save(advance));
    }

    @Override
    public List<SalaryAdvanceResponse> getAllAdvances() {
        return salaryAdvanceRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToAdvanceResponse).toList();
    }

    @Override
    public List<SalaryAdvanceResponse> getAdvancesByUser(Long userId) {
        return salaryAdvanceRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(userId, getCurrentTenantId())
                .stream().map(this::mapToAdvanceResponse).toList();
    }

    @Override
    public List<SalaryAdvanceResponse> getPendingAdvancesByUser(Long userId) {
        return salaryAdvanceRepository
                .findByUserIdAndTenantIdAndIsFullyRepaidFalseAndIsActiveTrue(
                        userId, getCurrentTenantId())
                .stream().map(this::mapToAdvanceResponse).toList();
    }

    @Override
    @Transactional
    public PayrollResponse generatePayroll(GeneratePayrollRequest request) {
        Long tenantId = getCurrentTenantId();

        if (payrollRepository.existsByUserIdAndTenantIdAndPayCycleStartDateAndIsActiveTrue(
                request.getUserId(), tenantId, request.getPayCycleStartDate())) {
            throw new FerosException("Payroll already generated for this user and pay cycle",
                    HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        // Resolve daily rate — use override if provided, else fetch from designation
        BigDecimal resolvedDailyRate = request.getDailyRate();
        if (resolvedDailyRate == null) {
            StaffProfile profile = staffProfileRepository
                    .findByUserIdAndTenantIdAndIsActiveTrue(request.getUserId(), tenantId)
                    .orElseThrow(() -> new FerosException(
                            "Staff profile not found for user. Cannot determine pay rate.", HttpStatus.BAD_REQUEST));
            if (profile.getDesignation() == null) {
                throw new FerosException(
                        "No designation assigned to this staff member. Cannot determine pay rate.", HttpStatus.BAD_REQUEST);
            }
            if (profile.getDesignation().getPayPerDay() == null) {
                throw new FerosException(
                        "Designation '" + profile.getDesignation().getName() + "' has no pay rate configured.", HttpStatus.BAD_REQUEST);
            }
            resolvedDailyRate = profile.getDesignation().getPayPerDay();
        }

        // Calculate attendance stats from attendance records
        List<com.feros.api.entity.Attendance> attendanceList = attendanceRepository
                .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrue(
                        request.getUserId(), tenantId,
                        request.getPayCycleStartDate(), request.getPayCycleEndDate());

        int totalDays = (int) ChronoUnit.DAYS.between(
                request.getPayCycleStartDate(), request.getPayCycleEndDate()) + 1;
        int presentDays = 0, absentDays = 0, halfDays = 0, leaveDays = 0;

        for (com.feros.api.entity.Attendance a : attendanceList) {
            String typeName = a.getAttendanceType().getName().toLowerCase();
            if (typeName.contains("present"))
                presentDays++;
            else if (typeName.contains("absent"))
                absentDays++;
            else if (typeName.contains("half"))
                halfDays++;
            else if (typeName.contains("leave"))
                leaveDays++;
        }

        // Calculate pay
        BigDecimal effectiveDays = BigDecimal.valueOf(presentDays)
                .add(BigDecimal.valueOf(halfDays).multiply(new BigDecimal("0.5")))
                .add(BigDecimal.valueOf(leaveDays));

        BigDecimal basicPay = resolvedDailyRate
                .multiply(effectiveDays)
                .setScale(2, RoundingMode.HALF_UP);

        // Overtime pay (per hour = dailyRate / 8 * 1.5)
        BigDecimal overtimePay = BigDecimal.ZERO;
        if (request.getOvertimeHours() != null &&
                request.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourlyRate = resolvedDailyRate
                    .divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);
            overtimePay = hourlyRate
                    .multiply(new BigDecimal("1.5"))
                    .multiply(request.getOvertimeHours())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal tripBonus = request.getTripBonus() != null ? request.getTripBonus() : BigDecimal.ZERO;
        BigDecimal grossPay = basicPay.add(overtimePay).add(tripBonus);

        // Build payroll
        Payroll payroll = Payroll.builder()
                .tenant(getCurrentTenant())
                .user(user)
                .payCycleStartDate(request.getPayCycleStartDate())
                .payCycleEndDate(request.getPayCycleEndDate())
                .totalDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .halfDays(halfDays)
                .leaveDays(leaveDays)
                .overtimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : BigDecimal.ZERO)
                .dailyRate(resolvedDailyRate)
                .basicPay(basicPay)
                .overtimePay(overtimePay)
                .tripBonus(tripBonus)
                .grossPay(grossPay)
                .totalDeductions(BigDecimal.ZERO)
                .netPay(grossPay)
                .payrollStatus(PayrollStatus.DRAFT)
                .remarks(request.getRemarks())
                .createdBy(getCurrentUser())
                .isActive(true)
                .build();

        Payroll savedPayroll = payrollRepository.save(payroll);

        // Process deductions
        BigDecimal totalDeductions = BigDecimal.ZERO;
        if (request.getDeductions() != null) {
            for (GeneratePayrollRequest.DeductionItem item : request.getDeductions()) {
                DeductionType deductionType = deductionTypeRepository
                        .findById(item.getDeductionTypeId())
                        .orElseThrow(() -> new FerosException("Deduction type not found",
                                HttpStatus.NOT_FOUND));

                PayrollDeduction deduction = PayrollDeduction.builder()
                        .tenant(getCurrentTenant())
                        .payroll(savedPayroll)
                        .deductionType(deductionType)
                        .amount(item.getAmount())
                        .remarks(item.getRemarks())
                        .isActive(true)
                        .build();

                // Link salary advance if provided and update repayment
                if (item.getSalaryAdvanceId() != null) {
                    SalaryAdvance advance = salaryAdvanceRepository
                            .findByIdAndTenantIdAndIsActiveTrue(
                                    item.getSalaryAdvanceId(), getCurrentTenantId())
                            .orElseThrow(() -> new FerosException("Salary advance not found",
                                    HttpStatus.NOT_FOUND));

                    deduction.setSalaryAdvance(advance);

                    // Update advance repayment
                    BigDecimal newRepaid = advance.getTotalRepaid().add(item.getAmount());
                    BigDecimal newBalance = advance.getAmount().subtract(newRepaid);
                    advance.setTotalRepaid(newRepaid);
                    advance.setBalanceAmount(newBalance.max(BigDecimal.ZERO));
                    advance.setIsFullyRepaid(newBalance.compareTo(BigDecimal.ZERO) <= 0);
                    salaryAdvanceRepository.save(advance);
                }

                payrollDeductionRepository.save(deduction);
                totalDeductions = totalDeductions.add(item.getAmount());
            }
        }

        // Update totals
        savedPayroll.setTotalDeductions(totalDeductions);
        savedPayroll.setNetPay(grossPay.subtract(totalDeductions));
        payrollRepository.save(savedPayroll);

        return mapToPayrollResponse(savedPayroll);
    }

    @Override
    public PayrollResponse getPayrollById(Long id) {
        return mapToPayrollResponse(payrollRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToPayrollResponse).toList();
    }

    @Override
    public List<PayrollResponse> getPayrollsByUser(Long userId) {
        return payrollRepository.findByUserIdAndTenantIdAndIsActiveTrue(userId, getCurrentTenantId())
                .stream().map(this::mapToPayrollResponse).toList();
    }

    @Override
    @Transactional
    public PayrollResponse approvePayroll(Long id, ApprovePayrollRequest request) {
        Payroll payroll = payrollRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND));

        if (payroll.getPayrollStatus() != PayrollStatus.DRAFT) {
            throw new FerosException("Only DRAFT payrolls can be approved", HttpStatus.BAD_REQUEST);
        }

        payroll.setPayrollStatus(PayrollStatus.PAID);
        payroll.setPaymentMode(request.getPaymentMode());
        payroll.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : TimeUtil.today());
        payroll.setReferenceNumber(request.getReferenceNumber());
        payroll.setApprovedBy(getCurrentUser());
        payroll.setApprovedAt(TimeUtil.nowIst());
        if (request.getRemarks() != null)
            payroll.setRemarks(request.getRemarks());

        Payroll saved = payrollRepository.save(payroll);

        notificationService.sendToUser(saved.getTenant(), saved.getUser(), NotificationType.PAYSLIP_GENERATED,
                "Payslip Ready",
                "Your payslip for " + saved.getPayCycleStartDate().getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                + " " + saved.getPayCycleStartDate().getYear() + " is ready. Net pay: \u20b9" + saved.getNetPay());

        return mapToPayrollResponse(saved);
    }

    @Override
    @Transactional
    public PayrollResponse cancelPayroll(Long id) {
        Payroll payroll = payrollRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND));

        if (payroll.getPayrollStatus() == PayrollStatus.PAID) {
            throw new FerosException("Cannot cancel a paid payroll", HttpStatus.BAD_REQUEST);
        }

        payroll.setPayrollStatus(PayrollStatus.CANCELLED);
        payroll.setIsActive(false);
        return mapToPayrollResponse(payrollRepository.save(payroll));
    }

    // ===================== MAPPERS =====================
    private SalaryAdvanceResponse mapToAdvanceResponse(SalaryAdvance a) {
        return SalaryAdvanceResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .userName(a.getUser().getName())
                .advanceDate(a.getAdvanceDate())
                .amount(a.getAmount())
                .reason(a.getReason())
                .totalRepaid(a.getTotalRepaid())
                .balanceAmount(a.getBalanceAmount())
                .isFullyRepaid(a.getIsFullyRepaid())
                .approvedById(a.getApprovedBy().getId())
                .approvedByName(a.getApprovedBy().getName())
                .approvedAt(a.getApprovedAt())
                .remarks(a.getRemarks())
                .isActive(a.getIsActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private PayrollResponse mapToPayrollResponse(Payroll p) {
        String roleName = p.getUser().getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse(null);

        List<PayrollDeductionResponse> deductions = payrollDeductionRepository
                .findByPayrollIdAndIsActiveTrue(p.getId())
                .stream().map(d -> PayrollDeductionResponse.builder()
                        .id(d.getId())
                        .deductionTypeId(d.getDeductionType().getId())
                        .deductionTypeName(d.getDeductionType().getName())
                        .amount(d.getAmount())
                        .salaryAdvanceId(d.getSalaryAdvance() != null ? d.getSalaryAdvance().getId() : null)
                        .remarks(d.getRemarks())
                        .build())
                .toList();

        return PayrollResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .userPhone(p.getUser().getPhone())
                .roleName(roleName)
                .payCycleStartDate(p.getPayCycleStartDate())
                .payCycleEndDate(p.getPayCycleEndDate())
                .totalDays(p.getTotalDays())
                .presentDays(p.getPresentDays())
                .absentDays(p.getAbsentDays())
                .halfDays(p.getHalfDays())
                .leaveDays(p.getLeaveDays())
                .overtimeHours(p.getOvertimeHours())
                .dailyRate(p.getDailyRate())
                .basicPay(p.getBasicPay())
                .overtimePay(p.getOvertimePay())
                .tripBonus(p.getTripBonus())
                .grossPay(p.getGrossPay())
                .totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .deductions(deductions)
                .paymentDate(p.getPaymentDate())
                .paymentMode(p.getPaymentMode())
                .referenceNumber(p.getReferenceNumber())
                .payrollStatus(p.getPayrollStatus())
                .approvedById(p.getApprovedBy() != null ? p.getApprovedBy().getId() : null)
                .approvedByName(p.getApprovedBy() != null ? p.getApprovedBy().getName() : null)
                .approvedAt(p.getApprovedAt())
                .remarks(p.getRemarks())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}