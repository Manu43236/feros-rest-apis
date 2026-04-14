package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class VehicleModelRequest {

    @NotNull(message = "Brand is required")
    private Long brandId;

    private Long vehicleTypeId;

    @NotBlank(message = "Model name is required")
    private String name;

    private Integer tyreCount;
    private BigDecimal capacityInTons;
}
