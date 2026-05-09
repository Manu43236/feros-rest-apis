package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TireCostPerKmResponse {
    private Long tireId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tireType;
    private BigDecimal purchaseCost;
    private BigDecimal totalLifetimeKm;
    private BigDecimal costPerKm;
}
