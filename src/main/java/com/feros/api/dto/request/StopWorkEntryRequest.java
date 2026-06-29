package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StopWorkEntryRequest {

    @NotNull(message = "End meter reading is required")
    private BigDecimal endMeter;

    private String notes;
}
