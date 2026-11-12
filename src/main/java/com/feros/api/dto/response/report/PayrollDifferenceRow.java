package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollDifferenceRow {
    private String employeeName;
    private String role;
    private BigDecimal grossPay;      // from payroll — the By Role figure
    private BigDecimal vehicleCost;   // pay attributable to a vehicle (same replay as Vehicle Cost)
    private BigDecimal difference;    // grossPay - vehicleCost
    private BigDecimal tripBonus;     // of the difference: trip bonus (never in Vehicle Cost)
    private BigDecimal overtimePay;   // of the difference: overtime (never in Vehicle Cost)
    private BigDecimal otherPay;      // of the difference: remainder (standby / un-attributed days)
}
