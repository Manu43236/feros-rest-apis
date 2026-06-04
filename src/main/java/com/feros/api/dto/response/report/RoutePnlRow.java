package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePnlRow {
    private String fromCity;
    private String toCity;
    private int totalTrips;
    private BigDecimal revenue;
    private BigDecimal tripExpenses;
    private BigDecimal netPnl;
}
