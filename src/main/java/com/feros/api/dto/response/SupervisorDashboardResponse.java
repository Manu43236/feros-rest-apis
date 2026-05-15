package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupervisorDashboardResponse {

    private OrderSummary   orders;
    private VehicleSummary vehicles;
    private StaffSummary   drivers;
    private StaffSummary   cleaners;
    private LrSummary      lrs;
    private AttendanceSummary attendance;
    private int            unreadNotifications;

    @Data @Builder
    public static class OrderSummary {
        private int total;
        private int pending;
        private int active;
        private int delivered;
        private int cancelled;
    }

    @Data @Builder
    public static class VehicleSummary {
        private int total;
        private int available;
        private int onTrip;
        private int breakdown;
        private int inactive;
    }

    @Data @Builder
    public static class StaffSummary {
        private int total;
        private int available;
        private int onTrip;
        private int todayPresent;
    }

    @Data @Builder
    public static class LrSummary {
        private int total;
        private int created;
        private int loaded;
        private int inTransit;
        private int delivered;
        private int cancelled;
    }

    @Data @Builder
    public static class AttendanceSummary {
        private int total;
        private int present;
        private int absent;
        private int halfDay;
        private int weeklyOff;
    }
}
