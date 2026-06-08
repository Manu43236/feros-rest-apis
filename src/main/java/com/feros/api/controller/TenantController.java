package com.feros.api.controller;

import com.feros.api.config.UserPrincipal;
import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.request.TenantSettingsUpdateRequest;
import com.feros.api.dto.request.UpdateMyTenantRequest;
import com.feros.api.dto.request.CreateUserRequest;
import com.feros.api.dto.response.TenantSettingsResponse;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.dto.response.TenantDocumentResponse;
import com.feros.api.dto.response.TenantResponse;
import com.feros.api.dto.response.UserResponse;
import com.feros.api.service.TenantService;
import com.feros.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

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

    @PostMapping("/{tenantId}/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUserForTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateUserRequest request) {
        request.setTenantId(tenantId);
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("User created successfully", response));
    }

    @PostMapping("/{tenantId}/users/bulk-upload")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUploadUsersForTenant(
            @PathVariable Long tenantId,
            @RequestParam("file") MultipartFile file) {
        BulkTenantUploadResponse response = userService.bulkUpload(file, tenantId);
        return ResponseEntity.ok(
                ApiResponse.success("Bulk upload completed", response));
    }

    // ─── Settings ──────────────────────────────────────────────────────────────

    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'SERVICE_MANAGER', 'STORE_KEEPER', 'DRIVER')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched", tenantService.getSettings()));
    }

    @PatchMapping("/my/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateSettings(
            @RequestBody TenantSettingsUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Settings updated", tenantService.updateSettings(request)));
    }

    // ─── My Tenant ─────────────────────────────────────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantResponse>> getMyTenant() {
        TenantResponse response = tenantService.getMyTenant();
        return ResponseEntity.ok(ApiResponse.success("Tenant fetched successfully", response));
    }

    @PutMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateMyTenant(
            @Valid @RequestBody UpdateMyTenantRequest request) {
        TenantResponse response = tenantService.updateMyTenant(request);
        return ResponseEntity.ok(ApiResponse.success("Company profile updated successfully", response));
    }

    // ─── Logo ──────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        TenantResponse response = tenantService.uploadLogo(id, file);
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", response));
    }

    @PostMapping("/my/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> uploadMyLogo(
            @RequestParam("file") MultipartFile file) {
        TenantResponse response = tenantService.uploadMyLogo(file);
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", response));
    }

    // ─── Documents ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantDocumentResponse>> addDocument(
            @PathVariable Long id,
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file) {
        TenantDocumentResponse response = tenantService.addDocument(id, documentName, file);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TenantDocumentResponse>>> getDocuments(
            @PathVariable Long id) {
        List<TenantDocumentResponse> response = tenantService.getDocuments(id);
        return ResponseEntity.ok(ApiResponse.success("Documents fetched successfully", response));
    }

    @DeleteMapping("/{id}/documents/{docId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        tenantService.deleteDocument(id, docId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    @PostMapping("/my/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TenantDocumentResponse>> addMyDocument(
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file) {
        TenantDocumentResponse response = tenantService.addMyDocument(documentName, file);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/my/documents")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TenantDocumentResponse>>> getMyDocuments() {
        List<TenantDocumentResponse> response = tenantService.getMyDocuments();
        return ResponseEntity.ok(ApiResponse.success("Documents fetched successfully", response));
    }

    @DeleteMapping("/my/documents/{docId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMyDocument(@PathVariable Long docId) {
        tenantService.deleteMyDocument(docId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    // ─── Impersonation ─────────────────────────────────────────────────────────

    @PostMapping("/{id}/impersonate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LoginResponse>> impersonate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        LoginResponse response = tenantService.impersonateTenant(id, principal.getUserId(), principal.getPhone());
        return ResponseEntity.ok(
                ApiResponse.success("Impersonation token issued", response));
    }
}