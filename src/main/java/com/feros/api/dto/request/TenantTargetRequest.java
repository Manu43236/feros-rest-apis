package com.feros.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TenantTargetRequest {

    @NotNull
    @Min(2020)
    private Integer year;

    @NotNull
    @Min(1) @Max(12)
    private Integer month;

    private Integer targetTrips;

    private BigDecimal targetTons;
}
