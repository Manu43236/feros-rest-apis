package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollYtdRow {
    private String employeeName;
    private String role;
    private String designation;
    private int totalPresentDays;
    private BigDecimal totalGrossPay;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetPay;
    // Bank details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
}
