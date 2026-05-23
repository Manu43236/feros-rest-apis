package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyreCostPerKmResponse {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tyreType;
    private BigDecimal purchaseCost;
    private BigDecimal totalLifetimeKm;
    private BigDecimal costPerKm;
}
