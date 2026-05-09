package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRevenueResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int tripCount;
    private BigDecimal totalLoadedTons;
    private BigDecimal estimatedRevenue;
}
