package com.feros.api.dto.request;

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
}
