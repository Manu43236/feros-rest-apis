package com.feros.api.service;

import com.feros.api.dto.request.GpsDeviceModelRequest;
import com.feros.api.dto.response.GpsDeviceModelResponse;

import java.util.List;

public interface GpsHardwareCatalogService {
    List<GpsDeviceModelResponse> getAll(String companyName);
    List<GpsDeviceModelResponse> getAllActive();
    GpsDeviceModelResponse getById(Long id);
    GpsDeviceModelResponse create(GpsDeviceModelRequest request);
    GpsDeviceModelResponse update(Long id, GpsDeviceModelRequest request);
    GpsDeviceModelResponse toggleActive(Long id);
}
