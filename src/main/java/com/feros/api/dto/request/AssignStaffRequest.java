package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignStaffRequest {

    @NotNull(message = "Vehicle allocation is required")
    private Long vehicleAllocationId;

    @NotNull(message = "User is required")
    private Long userId;

    /** DRIVER or CLEANER — validated against the user's actual role */
    private String slotRole;

    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private String remarks;
}