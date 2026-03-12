package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChargeTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}