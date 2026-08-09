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

    // null = auto-move existing D1+C1 to replacement vehicle
    // provided = supervisor chose specific staff; unselected staff are freed
    private Long selectedDriverId;
    private Long selectedCleanerId;

    private String notes;
}
