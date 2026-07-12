package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder
public class EquipmentAnalyticsResponse {
    private List<MachineAnalyticsRow> machines;
    private BigDecimal totalRevenue;
    private BigDecimal totalServiceCosts;
    private BigDecimal totalDepreciation;
    private BigDecimal totalNetProfit;
    private double avgUtilizationPct;
    private double avgAvailabilityPct;
}
