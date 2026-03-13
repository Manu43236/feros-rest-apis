package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AssignVehicleRequest {

    @NotNull(message = "Vehicle is required")
    private Long vehicleId;

    @NotNull(message = "Allocated weight is required")
    private BigDecimal allocatedWeight;

    private LocalDate expectedLoadDate;
    private LocalDate expectedDeliveryDate;
    private String remarks;
}