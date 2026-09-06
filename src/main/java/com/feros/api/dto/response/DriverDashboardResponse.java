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
    private java.time.LocalDateTime markedOutAt;
    private boolean canUndoOut;
    private String dutyLabel;
    private int unreadNotifications;
    private UpcomingTrip activeTrip;
    private List<UpcomingTrip> upcomingTrips;
    private AssignedVehicle assignedVehicle;
    private AssignedOrder assignedOrder;
    private ActiveLease activeLease;

    @Data
    @Builder
    public static class AssignedVehicle {
        private Long vehicleId;
        private String vehicleNumber;
        private String vehicleType;
    }

    @Data
    @Builder
    public static class AssignedOrder {
        private Long vehicleAllocationId;
        private Long orderId;
        private String vehicleNumber;
        private String clientName;
        private String fromCity;
        private String toCity;
        private java.time.LocalDate expectedLoadDate;
        private java.time.LocalDate expectedDeliveryDate;
    }

    @Data
    @Builder
    public static class ActiveLease {
        private Long leaseId;
        private Long assignmentId;
        private String leaseNumber;
        private String clientName;
        private String vehicleNumber;
        private String divisionName;
        private java.math.BigDecimal lastOdometer;
        // Active session info (null if no session running)
        private Long activeSessionId;
        private java.time.LocalDateTime sessionStartTime;
    }

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
        private java.time.LocalDateTime startOdometerRecordedAt;
        private java.math.BigDecimal loadedWeight;
        private java.time.LocalDate expectedLoadDate;
        private java.time.LocalDate expectedDeliveryDate;
        private String startedByName;
        private String startedByRole;
        private boolean hasActiveBreakdown;
    }
}
