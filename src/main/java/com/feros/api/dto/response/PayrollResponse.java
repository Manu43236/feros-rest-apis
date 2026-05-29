package com.feros.api.dto.response;

import com.feros.api.enums.PaymentMode;
import com.feros.api.enums.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String roleName;

    private LocalDate payCycleStartDate;
    private LocalDate payCycleEndDate;

    private Integer totalDays;
    private Integer presentDays;
    private Integer absentDays;
    private Integer halfDays;
    private Integer leaveDays;
    private BigDecimal overtimeHours;

    private BigDecimal dailyRate;
    private BigDecimal basicPay;
    private BigDecimal overtimePay;
    private BigDecimal tripBonus;
    private BigDecimal vehicleExtraPay;
    private BigDecimal grossPay;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;

    private List<PayrollDeductionResponse> deductions;

    private LocalDate paymentDate;
    private PaymentMode paymentMode;
    private String referenceNumber;
    private PayrollStatus payrollStatus;

    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;

    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}