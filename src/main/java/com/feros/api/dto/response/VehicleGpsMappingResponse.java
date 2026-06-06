package com.feros.api.dto.response;

import com.feros.api.entity.VehicleGpsMapping;
import com.feros.api.enums.GpsProviderType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehicleGpsMappingResponse {

    private Long id;
    private Long vehicleId;
    private String registrationNumber;
    private Long gpsProviderConfigId;
    private GpsProviderType providerType;
    private String providerDisplayName;
    private String providerVehicleId;
    private String providerRegNumber;
    private Boolean isActive;

    public static VehicleGpsMappingResponse from(VehicleGpsMapping mapping) {
        return VehicleGpsMappingResponse.builder()
                .id(mapping.getId())
                .vehicleId(mapping.getVehicle().getId())
                .registrationNumber(mapping.getVehicle().getRegistrationNumber())
                .gpsProviderConfigId(mapping.getGpsProviderConfig().getId())
                .providerType(mapping.getGpsProviderConfig().getProviderType())
                .providerDisplayName(mapping.getGpsProviderConfig().getDisplayName())
                .providerVehicleId(mapping.getProviderVehicleId())
                .providerRegNumber(mapping.getProviderRegNumber())
                .isActive(mapping.getIsActive())
                .build();
    }
}
