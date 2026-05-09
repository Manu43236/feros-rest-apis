package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripDurationResponse {
    private String fromCity;
    private String toCity;
    private int tripCount;
    private double avgDurationHours;
    private double minDurationHours;
    private double maxDurationHours;
}
