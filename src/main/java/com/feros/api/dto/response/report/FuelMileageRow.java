package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FuelMileageRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int fillCount;
    private BigDecimal totalLitresFilled;
    private BigDecimal totalFuelCost;
    private BigDecimal openingOdometer;
    private BigDecimal closingOdometer;
    private BigDecimal totalKm;
    private BigDecimal mileageKmPerLitre;
}
