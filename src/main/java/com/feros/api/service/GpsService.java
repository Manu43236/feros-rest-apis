package com.feros.api.service;

import com.feros.api.dto.request.GpsProviderConfigRequest;
import com.feros.api.dto.request.VehicleGpsMappingRequest;
import com.feros.api.dto.response.*;

import java.util.List;

public interface GpsService {

    // Provider config
    List<GpsProviderConfigResponse> getAllConfigs();
    GpsProviderConfigResponse createConfig(GpsProviderConfigRequest request);
    GpsProviderConfigResponse updateConfig(Long id, GpsProviderConfigRequest request);
    void deleteConfig(Long id);
    boolean testConnection(Long id);

    // Provider vehicles (for mapping screen)
    List<GpsProviderVehicleResponse> getProviderVehicles(Long configId);

    // Vehicle mappings
    List<VehicleGpsMappingResponse> getAllMappings();
    VehicleGpsMappingResponse createMapping(VehicleGpsMappingRequest request);
    void deleteMapping(Long id);

    // Fleet map
    List<GpsFleetVehicleResponse> getFleet();
}
