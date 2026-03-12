package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityRequest {

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotBlank(message = "City name is required")
    private String name;
}