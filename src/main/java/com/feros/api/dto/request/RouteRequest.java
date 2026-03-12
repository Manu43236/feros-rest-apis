package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class RouteRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Source city is required")
    private Long sourceCityId;

    @NotNull(message = "Destination city is required")
    private Long destinationCityId;

    private BigDecimal distanceInKm;
    private Integer estimatedHours;
}