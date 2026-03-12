package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PayRateRequest {
    @NotNull(message = "Designation is required")
    private Long designationId;

    private Long vehicleTypeId;

    @NotNull(message = "Pay per day is required")
    private BigDecimal payPerDay;

    @NotNull(message = "Effective from is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}