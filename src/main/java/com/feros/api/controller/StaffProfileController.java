package com.feros.api.controller;

import com.feros.api.dto.request.DocumentRequest;
import com.feros.api.dto.request.StaffProfileRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DocumentResponse;
import com.feros.api.dto.response.StaffProfileResponse;
import com.feros.api.dto.response.VehicleImageResponse;
import com.feros.api.service.StaffProfileService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffProfileController {

    private final StaffProfileService staffProfileService;

    // ===================== STAFF PROFILES =====================
    @GetMapping("/profiles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<StaffProfileResponse>>> getAllProfiles(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean equipmentOnly) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profiles fetched successfully", staffProfileService.getAllProfiles(equipmentOnly)));
    }

    @GetMapping("/profiles/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> getProfileByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile fetched successfully",
                staffProfileService.getProfileByUserId(userId)));
    }

    @PutMapping("/profiles/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> createOrUpdateProfile(
            @PathVariable Long userId, @Valid @RequestBody StaffProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile saved successfully",
                staffProfileService.createOrUpdateProfile(userId, request)));
    }

    // ===================== STAFF DOCUMENTS =====================
    @GetMapping("/{userId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getStaffDocuments(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Documents fetched successfully",
                staffProfileService.getStaffDocuments(userId)));
    }

    @PostMapping("/{userId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> addStaffDocument(
            @PathVariable Long userId, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document added successfully",
                staffProfileService.addStaffDocument(userId, request)));
    }

    @PutMapping("/documents/{documentId}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> verifyStaffDocument(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document verified successfully",
                staffProfileService.verifyStaffDocument(documentId)));
    }

    // ===================== VEHICLE DOCUMENTS =====================
    @GetMapping("/vehicles/{vehicleId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getVehicleDocuments(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Documents fetched successfully",
                staffProfileService.getVehicleDocuments(vehicleId)));
    }

    @PostMapping("/vehicles/{vehicleId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DocumentResponse>> addVehicleDocument(
            @PathVariable Long vehicleId, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document added successfully",
                staffProfileService.addVehicleDocument(vehicleId, request)));
    }

    @PutMapping("/vehicles/documents/{documentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateVehicleDocument(
            @PathVariable Long documentId,
            @RequestBody com.feros.api.dto.request.UpdateDocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document updated successfully",
                staffProfileService.updateVehicleDocument(documentId, request)));
    }

    @PutMapping("/vehicles/documents/{documentId}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DocumentResponse>> verifyVehicleDocument(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document verified successfully",
                staffProfileService.verifyVehicleDocument(documentId)));
    }

    @DeleteMapping("/vehicles/documents/{documentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleDocument(
            @PathVariable Long documentId) {
        staffProfileService.deleteVehicleDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    // ===================== VEHICLE IMAGES =====================
    @GetMapping("/vehicles/{vehicleId}/images")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<VehicleImageResponse>>> getVehicleImages(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Images fetched successfully",
                staffProfileService.getVehicleImages(vehicleId)));
    }

    @PostMapping("/vehicles/{vehicleId}/images")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<VehicleImageResponse>> addVehicleImage(
            @PathVariable Long vehicleId, @RequestBody AddImageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Image added successfully",
                staffProfileService.addVehicleImage(vehicleId, request.getImageUrl(), request.getCaption())));
    }

    @DeleteMapping("/vehicles/images/{imageId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleImage(@PathVariable Long imageId) {
        staffProfileService.deleteVehicleImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    @Getter
    @Setter
    static class AddImageRequest {
        private String imageUrl;
        private String caption;
    }
}