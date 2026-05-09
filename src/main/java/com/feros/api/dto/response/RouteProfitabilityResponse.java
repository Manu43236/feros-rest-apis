package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteProfitabilityResponse {
    private String fromCity;
    private String toCity;
    private int tripCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalCharges;
    private BigDecimal netProfit;
    private double profitMarginPct;
}
