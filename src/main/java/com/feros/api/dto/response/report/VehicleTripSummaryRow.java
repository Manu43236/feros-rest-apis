package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VehicleTripSummaryRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalTrips;
    private int completedTrips;
    private int inTransitTrips;
    private int cancelledTrips;
    private BigDecimal totalAllocatedWeight;
    private BigDecimal totalLoadedWeight;
    private BigDecimal totalDeliveredWeight;
}
