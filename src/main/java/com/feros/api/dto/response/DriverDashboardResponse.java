package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DriverDashboardResponse {
    private int totalTrips;
    private int pendingTrips;
    private boolean attendanceMarked;
    private boolean attendanceEnforced;
    private int unreadNotifications;
    private UpcomingTrip activeTrip;
    private List<UpcomingTrip> upcomingTrips;

    @Data
    @Builder
    public static class UpcomingTrip {
        private Long lrId;
        private Long orderId;
        private Long vehicleAllocationId;
        private String lrNumber;
        private String lrStatus;
        private String clientName;
        private String fromCity;
        private String toCity;
        private String vehicleNumber;
        private java.math.BigDecimal currentVehicleOdometer;
        private java.math.BigDecimal startOdometer;
        private java.math.BigDecimal loadedWeight;
        private java.time.LocalDate expectedLoadDate;
        private java.time.LocalDate expectedDeliveryDate;
        private String startedByName;
        private String startedByRole;
        private boolean hasActiveBreakdown;
    }
}
