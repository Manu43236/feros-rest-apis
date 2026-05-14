package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SupervisorDashboardResponse {
    private int totalOrders;
    private int activeOrders;
    private int pendingAssignments;
    private int todayPresent;
    private int unreadNotifications;
    private List<ActiveTripItem> activeTrips;

    @Data
    @Builder
    public static class ActiveTripItem {
        private Long lrId;
        private String lrNumber;
        private String vehicleNumber;
        private String clientName;
        private String fromCity;
        private String toCity;
        private java.time.LocalDate expectedDeliveryDate;
    }
}
