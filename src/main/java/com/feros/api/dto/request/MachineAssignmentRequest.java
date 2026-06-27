package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MachineAssignmentRequest {

    @NotNull(message = "Equipment is required")
    private Long equipmentId;

    private LocalDate startDate; // defaults to today if null
}
