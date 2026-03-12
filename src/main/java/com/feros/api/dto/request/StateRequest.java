package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateRequest {

    @NotBlank(message = "State name is required")
    private String name;

    @NotBlank(message = "State code is required")
    private String code;
}