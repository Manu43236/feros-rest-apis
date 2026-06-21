package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TyreCostRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int tyreCount;
    private BigDecimal purchaseCost;
    private BigDecimal retreadingCost;  // lifetime total across all retread cycles
    private BigDecimal totalCost;
}
