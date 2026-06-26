package com.feros.api.dto.request;

import com.feros.api.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentTypeRequest {

    @NotNull(message = "Model is required")
    private Long modelId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Default meter type is required")
    private MeterType defaultMeterType;
}
