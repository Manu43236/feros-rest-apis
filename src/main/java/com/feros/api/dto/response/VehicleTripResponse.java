package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VehicleTripResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String brand;
    private int totalTrips;
    private BigDecimal totalAllocatedWeight;
    private BigDecimal totalLoadedWeight;
    private BigDecimal totalDeliveredWeight;
}
