package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiclePnlRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private BigDecimal revenue;
    private BigDecimal tripExpenses;
    private BigDecimal fuelCost;
    private BigDecimal maintenanceCost;
    private BigDecimal tyreCost;
    private BigDecimal documentCost;
    private BigDecimal totalExpenses;
    private BigDecimal netPnl;
}
