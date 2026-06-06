package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryRegisterRow {
    private Long payrollId;
    private String employeeName;
    private String role;
    private String designation;
    private LocalDate payCycleStart;
    private LocalDate payCycleEnd;
    private int presentDays;
    private int absentDays;
    private int halfDays;
    private int leaveDays;
    private int totalDays;
    private BigDecimal dailyRate;
    private BigDecimal basicPay;
    private BigDecimal overtimePay;
    private BigDecimal tripBonus;
    private BigDecimal vehicleExtraPay;
    private BigDecimal grossPay;
    private String deductionsDetail; // e.g. "PF: 500, Advance: 1000"
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private String payrollStatus;
    private LocalDate paymentDate;
    private String paymentMode;
    private String referenceNumber;
    // Bank details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
}
