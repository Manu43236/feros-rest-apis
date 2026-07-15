package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.ApprovePayrollRequest;
import com.feros.api.dto.request.BulkGeneratePayrollRequest;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.dto.request.SalaryAdvanceRequest;
import com.feros.api.dto.request.UpdatePayrollRequest;
import com.feros.api.dto.response.BulkPayrollResult;
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
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.PayrollService;
import com.feros.api.util.NumberUtil;
import com.feros.api.enums.NotificationType;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.feros.api.enums.SalaryType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private final PlatformTransactionManager transactionManager;
    private final NumberGeneratorService numberGenerator;

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

    // ── Core generation logic (no @Transactional — callers manage transactions) ──

    private PayrollResponse generatePayrollCore(GeneratePayrollRequest request) {
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
            resolvedMonthlySalary = request.getMonthlySalary();
            if (resolvedMonthlySalary == null) {
                if (profile.getMonthlySalary() == null) {
                    throw new FerosException(
                            "No monthly salary configured for this staff member.", HttpStatus.BAD_REQUEST);
                }
                resolvedMonthlySalary = profile.getMonthlySalary();
            }

            int workingDays = countWorkingDaysInMonth(request.getPayCycleStartDate(), request.getPayCycleEndDate());
            if (workingDays == 0) workingDays = 1;

            BigDecimal lopDays = BigDecimal.valueOf(absentDays)
                    .add(BigDecimal.valueOf(halfDays).multiply(new BigDecimal("0.5")));

            BigDecimal perDayRate = resolvedMonthlySalary
                    .divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
            BigDecimal lopDeduction = perDayRate.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);

            basicPay = resolvedMonthlySalary.subtract(lopDeduction).setScale(2, RoundingMode.HALF_UP);
        } else {
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

        Payroll payroll = Payroll.builder()
                .tenant(getCurrentTenant())
                .referenceNumber(numberGenerator.generateMonthly(getCurrentTenantId(), NumberUtil.Type.PR))
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

                if (item.getSalaryAdvanceId() != null) {
                    SalaryAdvance advance = salaryAdvanceRepository
                            .findByIdAndTenantIdAndIsActiveTrue(
                                    item.getSalaryAdvanceId(), getCurrentTenantId())
                            .orElseThrow(() -> new FerosException("Salary advance not found",
                                    HttpStatus.NOT_FOUND));

                    deduction.setSalaryAdvance(advance);

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

        savedPayroll.setTotalDeductions(totalDeductions);
        savedPayroll.setNetPay(grossPay.subtract(totalDeductions));
        payrollRepository.save(savedPayroll);

        return mapToPayrollResponse(savedPayroll);
    }

    @Override
    @Transactional
    public PayrollResponse generatePayroll(GeneratePayrollRequest request) {
        return generatePayrollCore(request);
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
    public PayrollResponse updatePayroll(Long id, UpdatePayrollRequest request) {
        Long tenantId = getCurrentTenantId();

        Payroll payroll = payrollRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND));

        if (payroll.getPayrollStatus() != PayrollStatus.DRAFT) {
            throw new FerosException("Only DRAFT payrolls can be edited", HttpStatus.BAD_REQUEST);
        }

        SalaryType salaryType = payroll.getSalaryType();

        // Use request values if provided, otherwise keep existing
        BigDecimal newDailyRate      = request.getDailyRate()      != null ? request.getDailyRate()      : payroll.getDailyRate();
        BigDecimal newMonthlySalary  = request.getMonthlySalary()  != null ? request.getMonthlySalary()  : payroll.getMonthlySalary();
        BigDecimal newOvertimeHours  = request.getOvertimeHours()  != null ? request.getOvertimeHours()  : payroll.getOvertimeHours();
        BigDecimal newTripBonus      = request.getTripBonus()      != null ? request.getTripBonus()      : payroll.getTripBonus();

        // Recalculate basicPay using stored attendance counts + updated rate
        BigDecimal basicPay;
        if (salaryType == SalaryType.MONTHLY) {
            if (newMonthlySalary == null) {
                throw new FerosException("Monthly salary is required", HttpStatus.BAD_REQUEST);
            }
            int workingDays = countWorkingDaysInMonth(payroll.getPayCycleStartDate(), payroll.getPayCycleEndDate());
            if (workingDays == 0) workingDays = 1;

            BigDecimal lopDays = BigDecimal.valueOf(payroll.getAbsentDays())
                    .add(BigDecimal.valueOf(payroll.getHalfDays()).multiply(new BigDecimal("0.5")));
            BigDecimal perDayRate = newMonthlySalary.divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
            BigDecimal lopDeduction = perDayRate.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
            basicPay = newMonthlySalary.subtract(lopDeduction).setScale(2, RoundingMode.HALF_UP);
        } else {
            if (newDailyRate == null) {
                throw new FerosException("Daily rate is required", HttpStatus.BAD_REQUEST);
            }
            BigDecimal effectiveDays = BigDecimal.valueOf(payroll.getPresentDays())
                    .add(BigDecimal.valueOf(payroll.getHalfDays()).multiply(new BigDecimal("0.5")))
                    .add(BigDecimal.valueOf(payroll.getLeaveDays()));
            basicPay = newDailyRate.multiply(effectiveDays).setScale(2, RoundingMode.HALF_UP);
        }

        // Recalculate overtime pay
        BigDecimal overtimePay = BigDecimal.ZERO;
        if (newOvertimeHours != null && newOvertimeHours.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyRateForOt = newDailyRate;
            if (dailyRateForOt == null && newMonthlySalary != null) {
                int wd = countWorkingDaysInMonth(payroll.getPayCycleStartDate(), payroll.getPayCycleEndDate());
                dailyRateForOt = newMonthlySalary.divide(BigDecimal.valueOf(wd == 0 ? 1 : wd), 4, RoundingMode.HALF_UP);
            }
            if (dailyRateForOt != null) {
                BigDecimal hourlyRate = dailyRateForOt.divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);
                overtimePay = hourlyRate.multiply(new BigDecimal("1.5")).multiply(newOvertimeHours)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal vehicleExtraPay = payroll.getVehicleExtraPay() != null ? payroll.getVehicleExtraPay() : BigDecimal.ZERO;
        BigDecimal grossPay = basicPay.add(overtimePay)
                .add(newTripBonus != null ? newTripBonus : BigDecimal.ZERO)
                .add(vehicleExtraPay);

        payroll.setDailyRate(newDailyRate);
        payroll.setMonthlySalary(newMonthlySalary);
        payroll.setOvertimeHours(newOvertimeHours != null ? newOvertimeHours : BigDecimal.ZERO);
        payroll.setTripBonus(newTripBonus != null ? newTripBonus : BigDecimal.ZERO);
        payroll.setBasicPay(basicPay);
        payroll.setOvertimePay(overtimePay);
        payroll.setGrossPay(grossPay);
        if (request.getRemarks() != null) payroll.setRemarks(request.getRemarks());

        // Handle deductions: null = keep existing; list (even empty) = replace all
        BigDecimal totalDeductions;
        if (request.getDeductions() != null) {
            // Reverse existing advance repayments, then deactivate
            List<PayrollDeduction> existing = payrollDeductionRepository.findByPayrollIdAndIsActiveTrue(id);
            for (PayrollDeduction d : existing) {
                if (d.getSalaryAdvance() != null) {
                    SalaryAdvance adv = d.getSalaryAdvance();
                    BigDecimal newRepaid = adv.getTotalRepaid().subtract(d.getAmount()).max(BigDecimal.ZERO);
                    adv.setTotalRepaid(newRepaid);
                    adv.setBalanceAmount(adv.getAmount().subtract(newRepaid).max(BigDecimal.ZERO));
                    adv.setIsFullyRepaid(false);
                    salaryAdvanceRepository.save(adv);
                }
                d.setIsActive(false);
                payrollDeductionRepository.save(d);
            }

            // Create new deductions
            totalDeductions = BigDecimal.ZERO;
            for (UpdatePayrollRequest.DeductionItem item : request.getDeductions()) {
                DeductionType deductionType = deductionTypeRepository.findById(item.getDeductionTypeId())
                        .orElseThrow(() -> new FerosException("Deduction type not found", HttpStatus.NOT_FOUND));

                PayrollDeduction deduction = PayrollDeduction.builder()
                        .tenant(payroll.getTenant())
                        .payroll(payroll)
                        .deductionType(deductionType)
                        .amount(item.getAmount())
                        .remarks(item.getRemarks())
                        .isActive(true)
                        .build();

                if (item.getSalaryAdvanceId() != null) {
                    SalaryAdvance advance = salaryAdvanceRepository
                            .findByIdAndTenantIdAndIsActiveTrue(item.getSalaryAdvanceId(), tenantId)
                            .orElseThrow(() -> new FerosException("Salary advance not found", HttpStatus.NOT_FOUND));
                    deduction.setSalaryAdvance(advance);
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
        } else {
            // Keep existing — sum active deductions
            totalDeductions = payrollDeductionRepository.findByPayrollIdAndIsActiveTrue(id)
                    .stream().map(PayrollDeduction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(grossPay.subtract(totalDeductions));

        return mapToPayrollResponse(payrollRepository.save(payroll));
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

    @Override
    public BulkPayrollResult bulkGeneratePayroll(BulkGeneratePayrollRequest request) {
        // Each user's payroll runs in its own independent transaction so a failure
        // for one user does not roll back successful payrolls for others.
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        List<PayrollResponse> succeeded = new ArrayList<>();
        List<BulkPayrollResult.FailedEntry> failed = new ArrayList<>();

        for (Long userId : request.getUserIds()) {
            User user = userRepository.findById(userId).orElse(null);
            String userName = user != null ? user.getName() : "User #" + userId;

            try {
                PayrollResponse response = tt.execute(status -> {
                    GeneratePayrollRequest singleRequest = new GeneratePayrollRequest();
                    singleRequest.setUserId(userId);
                    singleRequest.setPayCycleStartDate(request.getPayCycleStartDate());
                    singleRequest.setPayCycleEndDate(request.getPayCycleEndDate());
                    return generatePayrollCore(singleRequest);
                });
                if (response != null) succeeded.add(response);
            } catch (Exception e) {
                String reason = e.getMessage() != null ? e.getMessage() : "Unexpected error";
                failed.add(BulkPayrollResult.FailedEntry.builder()
                        .userId(userId)
                        .userName(userName)
                        .reason(reason)
                        .build());
            }
        }

        return BulkPayrollResult.builder()
                .totalRequested(request.getUserIds().size())
                .successCount(succeeded.size())
                .failedCount(failed.size())
                .succeeded(succeeded)
                .failed(failed)
                .build();
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
