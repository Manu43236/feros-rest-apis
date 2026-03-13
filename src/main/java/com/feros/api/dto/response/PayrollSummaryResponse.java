package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PayrollSummaryResponse {
    private Long payrollId;
    private Long userId;
    private String userName;
    private String userPhone;
    private String roleName;
    private LocalDate payCycleStartDate;
    private LocalDate payCycleEndDate;
    private int totalDays;
    private int presentDays;
    private int absentDays;
    private int halfDays;
    private int leaveDays;
    private BigDecimal dailyRate;
    private BigDecimal basicPay;
    private BigDecimal overtimePay;
    private BigDecimal tripBonus;
    private BigDecimal grossPay;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private String payrollStatus;
}
