package com.feros.api.gps;

import com.feros.api.dto.response.GpsFleetVehicleResponse;
import com.feros.api.dto.response.GpsProviderVehicleResponse;
import com.feros.api.entity.GpsProviderConfig;
import com.feros.api.entity.VehicleGpsMapping;
import com.feros.api.enums.GpsProviderType;

import java.util.List;

/**
 * Common interface for all GPS provider integrations.
 * Each provider (TATA, Blackbuck, Vamosys, etc.) implements this interface.
 * The rest of FEROS only talks to this interface — never to provider-specific code directly.
 */
public interface GpsProviderAdapter {

    GpsProviderType getProviderType();

    /**
     * Test that the credentials are valid and the API is reachable.
     */
    boolean testConnection(GpsProviderConfig config);

    /**
     * Fetch all vehicles registered under this provider account.
     * Used in the vehicle mapping screen so admin can link them to FEROS vehicles.
     */
    List<GpsProviderVehicleResponse> fetchProviderVehicles(GpsProviderConfig config);

    /**
     * Fetch live location data for the mapped vehicles.
     */
    List<GpsFleetVehicleResponse> fetchLiveLocations(GpsProviderConfig config, List<VehicleGpsMapping> mappings);
}
