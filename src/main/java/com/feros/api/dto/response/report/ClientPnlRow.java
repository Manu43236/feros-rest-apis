package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPnlRow {
    private Long clientId;
    private String clientName;
    private BigDecimal totalInvoiced;
    private BigDecimal totalCollected;
    private BigDecimal balanceDue;
    private BigDecimal tripExpenses;
    private BigDecimal netPnl;
}
