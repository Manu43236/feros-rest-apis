package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Day-wise / vehicle-wise / session-wise annexure for a lease invoice.
 * Hierarchy: Lease → Date (session start date) → Vehicle → sessions.
 * Web/PDF only — additive, no DB change.
 */
@Getter @Setter @Builder
public class LeaseInvoiceAnnexureResponse {

    private List<LeaseGroup> leases;

    @Getter @Setter @Builder
    public static class LeaseGroup {
        private Long leaseId;
        private String leaseNumber;
        private List<DayGroup> days;
    }

    @Getter @Setter @Builder
    public static class DayGroup {
        private LocalDate date;
        private List<VehicleGroup> vehicles;
    }

    @Getter @Setter @Builder
    public static class VehicleGroup {
        private String registrationNumber;
        private List<SessionRow> sessions;
    }

    @Getter @Setter @Builder
    public static class SessionRow {
        private String divisionName;
        private String driverName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal kmDriven;
        private BigDecimal hoursWorked;
    }
}
