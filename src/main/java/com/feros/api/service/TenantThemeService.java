package com.feros.api.service;

import com.feros.api.dto.request.TenantThemeRequest;
import com.feros.api.dto.response.TenantThemeResponse;

public interface TenantThemeService {
    TenantThemeResponse getTheme(Long tenantId);

    TenantThemeResponse updateTheme(Long tenantId, TenantThemeRequest request);

    TenantThemeResponse getMyTheme();
}