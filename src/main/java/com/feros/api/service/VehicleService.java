package com.feros.api.service;

import com.feros.api.dto.request.VehicleRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.VehicleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest request);
    VehicleResponse getVehicleById(Long id);
    List<VehicleResponse> getAllVehicles();
    VehicleResponse updateVehicle(Long id, VehicleRequest request);
    VehicleResponse updateVehicleStatus(Long id, Long statusId);
    VehicleResponse toggleVehicleActive(Long id);
    void deleteVehicle(Long id);
    BulkTenantUploadResponse bulkUpload(MultipartFile file);
}