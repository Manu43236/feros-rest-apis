package com.feros.api.dto.request;

import com.feros.api.enums.ProviderSide;
import com.feros.api.enums.WorkOrderType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class WorkOrderRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    private String site;

    private BigDecimal mobilizationCharge;
    private BigDecimal demobilizationCharge;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    private LocalDate endDate;

    private Long parentWoId;
    private String notes;

    // KAN-16 commercial T&C (all optional)
    private Integer paymentTermsDays;
    private BigDecimal gstPercent;
    private BigDecimal retentionPercent;
    private BigDecimal tdsPercent;
    private Integer billingCycleMonths;
    private ProviderSide operatorByWhom;
    private ProviderSide dieselByWhom;
    private Integer workingHoursPerDay;
    private Boolean sundayWorking;
    private BigDecimal overtimeRateMultiplier;
    private String escalationClause;
    private String penaltyClause;
    // E5 KAN-28
    private Integer breakdownPenaltyThresholdHours;

    private WorkOrderType workOrderType;
    private BigDecimal agreedJobAmount;
    private String jobDescription;
}
