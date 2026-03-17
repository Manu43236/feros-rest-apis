package com.feros.api.service;

import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.dto.response.TenantResponse;
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
}