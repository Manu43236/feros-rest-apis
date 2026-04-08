package com.feros.api.service;

import com.feros.api.dto.request.FuelLogRequest;
import com.feros.api.dto.response.FuelLogResponse;

import java.util.List;

public interface FuelLogService {
    FuelLogResponse create(FuelLogRequest request);
    List<FuelLogResponse> getByVehicle(Long vehicleId);
    List<FuelLogResponse> getAll();
    FuelLogResponse getById(Long id);
    FuelLogResponse update(Long id, FuelLogRequest request);
    void delete(Long id);
}
