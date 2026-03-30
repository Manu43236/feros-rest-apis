package com.feros.api.dto.request;

import com.feros.api.enums.BreakdownDuration;
import com.feros.api.enums.BreakdownType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BreakdownRequest {

    @NotNull(message = "Breakdown type is required")
    private BreakdownType breakdownType;

    @NotNull(message = "Breakdown duration is required (SHORT or LONG)")
    private BreakdownDuration breakdownDuration;

    @NotNull(message = "Breakdown date is required")
    private LocalDateTime breakdownDate;

    private String location;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}
