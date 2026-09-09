package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsDeviceRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Model ID is required")
    private Long modelId;

    @NotBlank(message = "Device identifier is required")
    private String deviceIdentifier;

    private String credentials;
    private String notes;
}
