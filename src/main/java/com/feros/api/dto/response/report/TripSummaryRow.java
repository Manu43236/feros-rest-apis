package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripSummaryRow {
    private String orderNumber;
    private LocalDateTime orderCreatedAt;
    private String material;
    private String lrNumber;
    private LocalDateTime lrCreatedAt;
    private String registrationNumber;
    private LocalDateTime vehicleAssignedAt;
    private LocalDateTime tripStartTime;
    private LocalDateTime tripEndTime;
    private String driverName;
    private String lrStatus;
    private Double durationHours;
}
