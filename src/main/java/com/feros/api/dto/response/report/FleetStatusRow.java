package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FleetStatusRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String ownershipType;
    private String currentStatus;
    private String currentDriverName;
    private String currentCleanerName;
    private String tripScope;
}
