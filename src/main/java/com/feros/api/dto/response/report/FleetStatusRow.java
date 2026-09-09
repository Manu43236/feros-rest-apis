package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FleetStatusRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String currentStatus;
    /** "BREAKDOWN" or "GENERAL" — only set when currentStatus is IN_REPAIR */
    private String inRepairType;
}
