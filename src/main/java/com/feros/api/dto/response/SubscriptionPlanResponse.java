package com.feros.api.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private Integer maxLorries;
    private Integer maxUsers;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private BigDecimal pricePerVehicle;
    private Integer minVehicles;
    private Integer maxVehicles;
    // Feature flags
    private Boolean hasFuelLogs;
    private Boolean hasMeterReadings;
    private Boolean hasVehicleServices;
    private Boolean hasAttendance;
    private Boolean hasPayroll;
    private Boolean hasInventory;
    private Boolean hasReports;
    private Boolean hasCreditNotes;
    private String features;
    private Boolean isActive;
}
