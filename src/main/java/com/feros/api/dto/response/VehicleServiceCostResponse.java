package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleServiceCostResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int serviceCount;
    private BigDecimal totalServiceCost;
    private String lastServiceDate;
}
