package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnlSummaryRow {
    private BigDecimal totalInvoiced;
    private BigDecimal totalCollected;
    private BigDecimal balanceDue;
    private BigDecimal tripExpenses;
    private BigDecimal fuelExpenses;
    private BigDecimal maintenanceExpenses;
    private BigDecimal documentExpenses;
    private BigDecimal totalExpenses;
    private BigDecimal grossPnl;
    private BigDecimal netPnl;
}
