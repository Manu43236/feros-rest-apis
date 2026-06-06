package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleGpsMappingRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private Long gpsProviderConfigId;

    @NotBlank
    private String providerVehicleId;

    private String providerRegNumber;
}
