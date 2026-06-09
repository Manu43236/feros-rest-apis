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
import com.feros.api.entity.VehicleStaffAssignment;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.PayrollService;
import com.feros.api.enums.NotificationType;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.feros.api.enums.SalaryType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
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
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
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

    /**
     * Count working days (calendar days minus Sundays) in a given month.
     * Used as the denominator for monthly LOP deduction.
     */
    private int countWorkingDaysInMonth(LocalDate start, LocalDate end) {
        int workingDays = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            date = date.plusDays(1);
        }
        return workingDays;
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

        StaffProfile profile = staffProfileRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(request.getUserId(), tenantId)
                .orElseThrow(() -> new FerosException(
                        "Staff profile not found for user. Cannot determine pay rate.", HttpStatus.BAD_REQUEST));

        SalaryType salaryType = profile.getSalaryType() != null ? profile.getSalaryType() : SalaryType.DAILY;

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

        BigDecimal basicPay;
        BigDecimal resolvedDailyRate = null;
        BigDecimal resolvedMonthlySalary = null;

        if (salaryType == SalaryType.MONTHLY) {
            // Resolve monthly salary — use override if provided, else fetch from profile
            resolvedMonthlySalary = request.getMonthlySalary();
            if (resolvedMonthlySalary == null) {
                if (profile.getMonthlySalary() == null) {
                    throw new FerosException(
                            "No monthly salary configured for this staff member.", HttpStatus.BAD_REQUEST);
                }
                resolvedMonthlySalary = profile.getMonthlySalary();
            }

            // LOP deduction: (monthlySalary / workingDays) × lopDays
            // lopDays = absent days only (leave days are paid, half days deduct 0.5)
            int workingDays = countWorkingDaysInMonth(request.getPayCycleStartDate(), request.getPayCycleEndDate());
            if (workingDays == 0) workingDays = 1; // safety guard

            BigDecimal lopDays = BigDecimal.valueOf(absentDays)
                    .add(BigDecimal.valueOf(halfDays).multiply(new BigDecimal("0.5")));

            BigDecimal perDayRate = resolvedMonthlySalary
                    .divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
            BigDecimal lopDeduction = perDayRate.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);

            basicPay = resolvedMonthlySalary.subtract(lopDeduction).setScale(2, RoundingMode.HALF_UP);
        } else {
            // DAILY — existing logic
            resolvedDailyRate = request.getDailyRate();
            if (resolvedDailyRate == null) {
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

            BigDecimal effectiveDays = BigDecimal.valueOf(presentDays)
                    .add(BigDecimal.valueOf(halfDays).multiply(new BigDecimal("0.5")))
                    .add(BigDecimal.valueOf(leaveDays));

            basicPay = resolvedDailyRate
                    .multiply(effectiveDays)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Overtime pay — based on daily rate equivalent (monthly / workingDays if monthly)
        BigDecimal overtimePay = BigDecimal.ZERO;
        if (request.getOvertimeHours() != null &&
                request.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyRateForOt = resolvedDailyRate;
            if (dailyRateForOt == null && resolvedMonthlySalary != null) {
                int wd = countWorkingDaysInMonth(request.getPayCycleStartDate(), request.getPayCycleEndDate());
                dailyRateForOt = resolvedMonthlySalary.divide(BigDecimal.valueOf(wd == 0 ? 1 : wd), 4, RoundingMode.HALF_UP);
            }
            if (dailyRateForOt != null) {
                BigDecimal hourlyRate = dailyRateForOt.divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);
                overtimePay = hourlyRate
                        .multiply(new BigDecimal("1.5"))
                        .multiply(request.getOvertimeHours())
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal tripBonus = request.getTripBonus() != null ? request.getTripBonus() : BigDecimal.ZERO;

        // Vehicle extra pay — sum extraPayPerDay × present days in each vehicle assignment period
        BigDecimal vehicleExtraPay = BigDecimal.ZERO;
        try {
            List<VehicleStaffAssignment> assignments = vehicleStaffAssignmentRepository
                    .findOverlappingByUser(request.getUserId(), tenantId,
                            request.getPayCycleStartDate(), request.getPayCycleEndDate());
            for (VehicleStaffAssignment assignment : assignments) {
                if (!Boolean.TRUE.equals(assignment.getVehicle().getExtraPayEnabled())
                        || assignment.getVehicle().getExtraPayPerDay() == null) continue;

                LocalDate from = assignment.getAssignedFrom()
                        .isBefore(request.getPayCycleStartDate())
                        ? request.getPayCycleStartDate() : assignment.getAssignedFrom();
                LocalDate to = (assignment.getAssignedTo() == null
                        || assignment.getAssignedTo().isAfter(request.getPayCycleEndDate()))
                        ? request.getPayCycleEndDate() : assignment.getAssignedTo();

                long presentInWindow = attendanceList.stream()
                        .filter(a -> {
                            LocalDate d = a.getAttendanceDate();
                            return !d.isBefore(from) && !d.isAfter(to)
                                    && a.getAttendanceType().getName().toLowerCase().contains("present");
                        })
                        .count();

                vehicleExtraPay = vehicleExtraPay.add(
                        assignment.getVehicle().getExtraPayPerDay()
                                .multiply(BigDecimal.valueOf(presentInWindow)));
            }
        } catch (Exception e) {
            vehicleExtraPay = BigDecimal.ZERO;
        }
        vehicleExtraPay = vehicleExtraPay.setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossPay = basicPay.add(overtimePay).add(tripBonus).add(vehicleExtraPay);

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
                .salaryType(salaryType)
                .dailyRate(resolvedDailyRate)
                .monthlySalary(resolvedMonthlySalary)
                .basicPay(basicPay)
                .overtimePay(overtimePay)
                .tripBonus(tripBonus)
                .vehicleExtraPay(vehicleExtraPay)
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
    public Page<PayrollResponse> getAllPayrolls(int page, int size, String search) {
        Long tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return payrollRepository.findAllPaged(tenantId, searchParam, pageable)
                .map(this::mapToPayrollResponse);
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

        String designationName = staffProfileRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(p.getUser().getId(), p.getTenant().getId())
                .map(sp -> sp.getDesignation() != null ? sp.getDesignation().getName() : null)
                .orElse(null);

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
                .designationName(designationName)
                .payCycleStartDate(p.getPayCycleStartDate())
                .payCycleEndDate(p.getPayCycleEndDate())
                .totalDays(p.getTotalDays())
                .presentDays(p.getPresentDays())
                .absentDays(p.getAbsentDays())
                .halfDays(p.getHalfDays())
                .leaveDays(p.getLeaveDays())
                .overtimeHours(p.getOvertimeHours())
                .salaryType(p.getSalaryType())
                .dailyRate(p.getDailyRate())
                .monthlySalary(p.getMonthlySalary())
                .basicPay(p.getBasicPay())
                .overtimePay(p.getOvertimePay())
                .tripBonus(p.getTripBonus())
                .vehicleExtraPay(p.getVehicleExtraPay())
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