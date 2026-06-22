package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DashboardResponse {

    private OrderSummary orders;
    private VehicleSummary vehicles;
    private InvoiceSummary invoices;
    private AttendanceSummary todayAttendance;

    @Data
    @Builder
    public static class OrderSummary {
        private long total;
        private long pending;
        private long partiallyAssigned;
        private long fullyAssigned;
        private long inTransit;
        private long partiallyDelivered;
        private long delivered;
        private long cancelled;
    }

    @Data
    @Builder
    public static class VehicleSummary {
        private long total;
        private long available;
        private long assigned;
        private long onTrip;
        private long underMaintenance;
        private long breakdown;
        private long inactive;
    }

    @Data
    @Builder
    public static class InvoiceSummary {
        private long draft;
        private long sent;
        private long partiallyPaid;
        private long overdue;
        private long paid;
        private BigDecimal totalOutstanding;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    public static class AttendanceSummary {
        private LocalDate date;
        private long present;
        private long absent;
        private long halfDay;
        private long onLeave;
        private long total;
    }
}
