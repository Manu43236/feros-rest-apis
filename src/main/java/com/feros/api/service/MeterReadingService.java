package com.feros.api.service;

import com.feros.api.dto.request.MeterReadingRequest;
import com.feros.api.dto.response.MeterReadingResponse;

import java.util.List;

public interface MeterReadingService {
    MeterReadingResponse create(MeterReadingRequest request);
    List<MeterReadingResponse> getAll(Long vehicleId);
    void delete(Long id);
}
