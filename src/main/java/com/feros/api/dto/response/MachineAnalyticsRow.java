package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class MachineAnalyticsRow {
    private Long equipmentId;
    private String serialNumber;
    private String equipmentTypeName;
    private String makeName;
    private int deployedDays;
    private double shiftHours;
    private double workingHours;
    private double breakdownHours;
    private double utilizationPct;
    private double availabilityPct;
    private BigDecimal revenue;
    private BigDecimal serviceCosts;
    private BigDecimal depreciation;
    private BigDecimal netProfit;
}
