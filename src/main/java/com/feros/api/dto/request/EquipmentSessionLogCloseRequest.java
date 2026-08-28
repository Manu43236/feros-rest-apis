package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EquipmentSessionLogCloseRequest {

    // Defaults to now on the backend if not provided
    private LocalDateTime endTime;

    @NotNull(message = "End HMR is required")
    private BigDecimal endHmr;

    private BigDecimal fuelConsumed;

    private String notes;
}
