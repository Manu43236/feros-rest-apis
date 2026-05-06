package com.feros.api.service;

import com.feros.api.controller.VehicleController.UpdateStatusRequest;
import com.feros.api.dto.request.VehicleRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.VehicleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest request);
    VehicleResponse getVehicleById(Long id);
    List<VehicleResponse> getAllVehicles(LocalDate date);
    VehicleResponse updateVehicle(Long id, VehicleRequest request);
    VehicleResponse updateVehicleStatus(Long id, UpdateStatusRequest request);
    VehicleResponse toggleVehicleActive(Long id);
    void deleteVehicle(Long id);
    BulkTenantUploadResponse bulkUpload(MultipartFile file);
    int backfillTirePositions();
}