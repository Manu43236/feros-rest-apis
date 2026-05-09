package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DailyVehicleActivityResponse {
    private int onRoadCount;
    private int startedTodayCount;
    private int deliveredTodayCount;
    private int idleCount;
    private List<VehicleActivityRow> onRoad;
    private List<VehicleActivityRow> startedToday;
    private List<VehicleActivityRow> deliveredToday;
    private List<VehicleActivityRow> idle;

    @Data
    @Builder
    public static class VehicleActivityRow {
        private Long vehicleId;
        private String registrationNumber;
        private String vehicleType;
        private String clientName;
        private String fromCity;
        private String toCity;
        private String lrNumber;
        private String loadedAt;
        private String deliveredAt;
    }
}
