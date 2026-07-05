package com.feros.api.dto.request;

import com.feros.api.enums.RateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VehicleLeaseRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    private String site;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Rate type is required")
    private RateType rateType;

    private String notes;
}
