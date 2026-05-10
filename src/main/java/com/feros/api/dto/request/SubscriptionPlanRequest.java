package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;
    private BigDecimal pricePerVehicle;
    private Integer minVehicles;
    private Integer maxVehicles;
    private Integer maxLorries;
    private Integer maxUsers;
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
}
