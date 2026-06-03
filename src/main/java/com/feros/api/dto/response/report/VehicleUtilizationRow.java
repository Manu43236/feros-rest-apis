package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class VehicleUtilizationRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalTrips;
    private long daysOnTrip;
    private int totalDaysInPeriod;
    private double utilizationPercent;
    private LocalDate lastTripDate;
}
