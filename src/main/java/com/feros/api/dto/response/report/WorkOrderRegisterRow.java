package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class WorkOrderRegisterRow {
    private Long woId;
    private String woNumber;
    private String clientName;
    private String site;
    private String workOrderType;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer machineCount;
    private BigDecimal mobilizationCharge;
    private BigDecimal demobilizationCharge;
    private BigDecimal agreedJobAmount;
    private BigDecimal gstPercent;
    private BigDecimal retentionPercent;
    private BigDecimal tdsPercent;
    private Integer billingCycleMonths;
    // Days until endDate for live WOs (negative = past end). null when no endDate or terminal status.
    private Long daysRemaining;
    private Boolean overdue; // live WO past its endDate
}
