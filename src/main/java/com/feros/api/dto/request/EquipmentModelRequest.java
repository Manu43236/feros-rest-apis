package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentModelRequest {

    @NotNull(message = "Make is required")
    private Long makeId;

    @NotBlank(message = "Name is required")
    private String name;
}
