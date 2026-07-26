package com.feros.api.service;

import com.feros.api.dto.request.DocumentRequest;
import com.feros.api.dto.request.StaffProfileRequest;
import com.feros.api.dto.request.UpdateDocumentRequest;
import com.feros.api.dto.response.DocumentResponse;
import com.feros.api.dto.response.DriverDocsResponse;
import com.feros.api.dto.response.StaffProfileResponse;
import com.feros.api.dto.response.VehicleImageResponse;

import java.util.List;

public interface StaffProfileService {
    StaffProfileResponse createOrUpdateProfile(Long userId, StaffProfileRequest request);
    StaffProfileResponse getProfileByUserId(Long userId);
    List<StaffProfileResponse> getAllProfiles();
    List<StaffProfileResponse> getAllProfiles(boolean equipmentOnly);

    DocumentResponse addStaffDocument(Long userId, DocumentRequest request);
    List<DocumentResponse> getStaffDocuments(Long userId);
    DocumentResponse verifyStaffDocument(Long documentId);

    DocumentResponse addVehicleDocument(Long vehicleId, DocumentRequest request);
    DocumentResponse updateVehicleDocument(Long documentId, UpdateDocumentRequest request);
    List<DocumentResponse> getVehicleDocuments(Long vehicleId);
    DocumentResponse verifyVehicleDocument(Long documentId);
    void deleteVehicleDocument(Long documentId);

    DriverDocsResponse getDriverDocs(Long vehicleId);

    VehicleImageResponse addVehicleImage(Long vehicleId, String imageUrl, String caption);
    List<VehicleImageResponse> getVehicleImages(Long vehicleId);
    void deleteVehicleImage(Long imageId);
}