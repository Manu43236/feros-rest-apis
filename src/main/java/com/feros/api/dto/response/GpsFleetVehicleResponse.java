package com.feros.api.dto.response;

import com.feros.api.enums.GpsProviderType;
import com.feros.api.enums.GpsVehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Live GPS data for one vehicle — returned by the fleet map endpoint.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpsFleetVehicleResponse {

    private Long vehicleId;
    private String registrationNumber;
    private String driverName;

    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Boolean ignitionOn;

    private GpsVehicleStatus gpsStatus;
    private LocalDateTime lastUpdatedAt;

    private GpsProviderType providerType;
    private String providerVehicleId;
}
