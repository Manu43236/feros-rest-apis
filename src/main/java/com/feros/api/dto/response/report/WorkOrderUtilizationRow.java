package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WorkOrderUtilizationRow {
    private Long woId;
    private String woNumber;
    private String clientName;
    private Integer daysLogged;
    private BigDecimal workingHours;
    private BigDecimal idleHours;
    private BigDecimal standbyHours;
    private BigDecimal breakdownHours;
    private BigDecimal totalHours;
    // working / (working + idle + standby + breakdown), as a percentage.
    private BigDecimal utilizationPct;
    private Integer penaltyThresholdHours;
    private Boolean penaltyBreached; // breakdownHours > threshold
}
