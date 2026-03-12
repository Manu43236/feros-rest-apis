package com.feros.api.controller;

import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.TenantResponse;
import com.feros.api.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.ok(
                ApiResponse.success("Tenant created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants() {
        List<TenantResponse> response = tenantService.getAllTenants();
        return ResponseEntity.ok(
                ApiResponse.success("Tenants fetched successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(
            @PathVariable Long id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Tenant fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Tenant updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(
                ApiResponse.success("Tenant deleted successfully", null));
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUpload(
            @RequestParam("file") MultipartFile file) {
        BulkTenantUploadResponse response = tenantService.bulkUpload(file);
        return ResponseEntity.ok(
                ApiResponse.success("Bulk upload completed", response));
    }
}