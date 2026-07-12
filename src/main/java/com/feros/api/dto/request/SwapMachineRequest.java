package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SwapMachineRequest {

    @NotNull(message = "New equipment ID is required")
    private Long newEquipmentId;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String reason;
}
