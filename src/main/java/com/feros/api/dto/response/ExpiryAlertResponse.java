package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ExpiryAlertResponse {

    private List<VehicleAlert> vehicleAlerts;
    private List<StaffDocumentAlert> staffDocumentAlerts;
    private int totalAlerts;

    @Data
    @Builder
    public static class VehicleAlert {
        private Long vehicleId;
        private String registrationNumber;
        private String alertType;   // INSURANCE, PERMIT, FITNESS, PUC, ROAD_TAX, RC, DOCUMENT
        private String documentName; // for document_type based alerts
        private LocalDate expiryDate;
        private long daysLeft;      // negative means already expired
        private boolean expired;
    }

    @Data
    @Builder
    public static class StaffDocumentAlert {
        private Long userId;
        private String userName;
        private String documentType;
        private String documentNumber;
        private LocalDate expiryDate;
        private long daysLeft;
        private boolean expired;
    }
}
