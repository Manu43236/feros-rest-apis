package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VehicleTypeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    private BigDecimal capacityInTons;

    private Integer tyreCount;
}