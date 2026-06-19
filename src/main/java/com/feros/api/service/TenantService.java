package com.feros.api.service;

import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.request.TenantSettingsUpdateRequest;
import com.feros.api.dto.request.UpdateMyTenantRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.dto.response.TenantDocumentResponse;
import com.feros.api.dto.response.TenantResponse;
import com.feros.api.dto.response.TenantSettingsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    TenantResponse getTenantById(Long id);
    List<TenantResponse> getAllTenants();
    TenantResponse updateTenant(Long id, CreateTenantRequest request);
    void deleteTenant(Long id);
    BulkTenantUploadResponse bulkUpload(MultipartFile file);
    LoginResponse impersonateTenant(Long tenantId, Long saUserId, String saPhone);
    TenantResponse getMyTenant();
    TenantResponse updateMyTenant(UpdateMyTenantRequest request);

    // Settings
    TenantSettingsResponse getSettings();
    TenantSettingsResponse updateSettings(TenantSettingsUpdateRequest request);

    // Subscription overrides
    TenantResponse updateUserLimit(Long tenantId, Integer customUserLimit);

    // S3 — Logo
    TenantResponse uploadLogo(Long tenantId, MultipartFile file);
    TenantResponse uploadMyLogo(MultipartFile file);

    // S3 — Documents
    TenantDocumentResponse addDocument(Long tenantId, String documentName, MultipartFile file);
    TenantDocumentResponse addMyDocument(String documentName, MultipartFile file);
    List<TenantDocumentResponse> getDocuments(Long tenantId);
    List<TenantDocumentResponse> getMyDocuments();
    void deleteDocument(Long tenantId, Long docId);
    void deleteMyDocument(Long docId);
}