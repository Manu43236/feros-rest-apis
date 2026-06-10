package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumptionByVehicleRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private Long partId;
    private String partName;
    private String partCategory;
    private int timesConsumed;
    private BigDecimal totalCost;
}
