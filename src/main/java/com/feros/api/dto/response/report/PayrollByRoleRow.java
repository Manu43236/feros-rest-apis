package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollByRoleRow {
    private String role;
    private int employeeCount;
    private BigDecimal totalGrossPay;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetPay;
}
