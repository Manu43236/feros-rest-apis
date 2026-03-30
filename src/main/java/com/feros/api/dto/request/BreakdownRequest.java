package com.feros.api.dto.request;

import com.feros.api.enums.BreakdownType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BreakdownRequest {

    @NotNull(message = "Breakdown type is required")
    private BreakdownType breakdownType;

    @NotNull(message = "Breakdown date is required")
    private LocalDateTime breakdownDate;

    private String location;
    private String reason;
    private String notes;
}
