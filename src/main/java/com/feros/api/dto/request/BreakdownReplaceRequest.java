package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BreakdownReplaceRequest {

    @NotNull(message = "Replacement vehicle is required")
    private Long replacementVehicleId;

    private LocalDate expectedDeliveryDate;

    // true = transfer existing driver+cleaner to replacement vehicle
    // false = cancel existing staff, admin assigns manually
    @NotNull(message = "transferStaff flag is required")
    private Boolean transferStaff;

    private String notes;
}
