package com.feros.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A vehicle as returned by a GPS provider's API.
 * Used in the vehicle mapping screen so admin can link provider vehicles to FEROS vehicles.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpsProviderVehicleResponse {

    private String providerVehicleId;
    private String registrationNumber;
    private String vehicleModel;

    // Set when auto-matched to a FEROS vehicle by registration number
    private Long ferosVehicleId;
    private Boolean autoMatched;
}
