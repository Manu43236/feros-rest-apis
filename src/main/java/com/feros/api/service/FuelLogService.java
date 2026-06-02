package com.feros.api.service;

import com.feros.api.dto.request.FuelLogRequest;
import com.feros.api.dto.response.FuelLogResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FuelLogService {
    FuelLogResponse create(FuelLogRequest request);
    List<FuelLogResponse> getByVehicle(Long vehicleId);
    Page<FuelLogResponse> getAll(int page, int size, Long vehicleId, String paymentMode, Boolean fullTank, String search);
    FuelLogResponse getById(Long id);
    FuelLogResponse update(Long id, FuelLogRequest request);
    void delete(Long id);
}
