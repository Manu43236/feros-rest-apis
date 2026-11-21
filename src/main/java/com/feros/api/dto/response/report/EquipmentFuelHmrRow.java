package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentFuelHmrRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String equipmentType;
    private String meterType;
    private Integer fillCount;
    private BigDecimal totalLitresFilled;
    private BigDecimal totalFuelCost;
    private BigDecimal openingHmr;
    private BigDecimal closingHmr;
    private BigDecimal totalHours;
    // Equipment efficiency is consumption (litres burned per hour), not distance mileage.
    private BigDecimal litresPerHour;
}
