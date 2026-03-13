package com.feros.api.service;

import com.feros.api.dto.request.VehicleRequest;
import com.feros.api.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest request);
    VehicleResponse getVehicleById(Long id);
    List<VehicleResponse> getAllVehicles();
    VehicleResponse updateVehicle(Long id, VehicleRequest request);
    void deleteVehicle(Long id);
}