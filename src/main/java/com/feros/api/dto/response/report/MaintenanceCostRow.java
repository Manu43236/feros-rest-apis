package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MaintenanceCostRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalServices;
    private BigDecimal serviceCost;
    private BigDecimal sparePartsCost;
    private BigDecimal totalCost;
}
