package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LocalLongTripSummaryResponse {
    private int localCount;
    private int longDistanceCount;
    private int totalToday;
    private List<TripRow> trips;

    @Data
    @Builder
    public static class TripRow {
        private String tripType; // LOCAL or LONG
        private String lrNumber;
        private String registrationNumber;
        private String clientName;
        private String fromCity;
        private String fromState;
        private String toCity;
        private String toState;
    }
}
