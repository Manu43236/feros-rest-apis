package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LrChargeRequest {

    @NotNull(message = "Charge type is required")
    private Long chargeTypeId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String remarks;
}