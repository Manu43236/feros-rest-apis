package com.feros.api.dto.request;

import com.feros.api.enums.VehicleStatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleStatusRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Status type is required")
    private VehicleStatusType statusType;
}
