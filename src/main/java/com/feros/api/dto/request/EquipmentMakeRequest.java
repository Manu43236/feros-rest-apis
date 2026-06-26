package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentMakeRequest {

    @NotBlank(message = "Name is required")
    private String name;
}
