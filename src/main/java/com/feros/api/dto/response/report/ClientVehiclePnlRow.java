package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientVehiclePnlRow {
    private Long clientId;
    private String clientName;
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalTrips;
    private BigDecimal revenue;
    private BigDecimal tripExpenses;
    private BigDecimal netPnl;
}
