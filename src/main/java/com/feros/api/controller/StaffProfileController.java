package com.feros.api.controller;

import com.feros.api.dto.request.DocumentRequest;
import com.feros.api.dto.request.StaffProfileRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DocumentResponse;
import com.feros.api.dto.response.StaffProfileResponse;
import com.feros.api.service.StaffProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<StaffProfileResponse>>> getAllProfiles() {
        return ResponseEntity.ok(ApiResponse.success(
                "Profiles fetched successfully", staffProfileService.getAllProfiles()));
    }

    @GetMapping("/profiles/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getVehicleDocuments(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Documents fetched successfully",
                staffProfileService.getVehicleDocuments(vehicleId)));
    }

    @PostMapping("/vehicles/{vehicleId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> addVehicleDocument(
            @PathVariable Long vehicleId, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document added successfully",
                staffProfileService.addVehicleDocument(vehicleId, request)));
    }

    @PutMapping("/vehicles/documents/{documentId}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> verifyVehicleDocument(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document verified successfully",
                staffProfileService.verifyVehicleDocument(documentId)));
    }
}