package com.feros.api.service;

import com.feros.api.dto.request.GpsDeviceRequest;
import com.feros.api.dto.response.GpsDeviceResponse;

import java.util.List;

public interface GpsDeviceService {
    List<GpsDeviceResponse> getAll();
    GpsDeviceResponse getByVehicle(Long vehicleId);
    GpsDeviceResponse register(GpsDeviceRequest request);
    GpsDeviceResponse update(Long id, GpsDeviceRequest request);
    GpsDeviceResponse deactivate(Long id);
}
