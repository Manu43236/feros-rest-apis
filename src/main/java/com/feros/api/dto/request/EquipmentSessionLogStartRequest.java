package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EquipmentSessionLogStartRequest {

    // Defaults to now on the backend if not provided
    private LocalDateTime startTime;

    @NotNull(message = "Start HMR is required")
    private BigDecimal startHmr;

    private String notes;
}
