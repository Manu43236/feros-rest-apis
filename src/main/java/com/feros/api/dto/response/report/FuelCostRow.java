package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FuelCostRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalFills;
    private BigDecimal totalLitres;
    private BigDecimal totalCost;
}
