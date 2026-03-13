package com.feros.api.service.impl;

import com.feros.api.dto.request.TenantThemeRequest;
import com.feros.api.dto.response.TenantThemeResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.TenantTheme;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.TenantThemeRepository;
import com.feros.api.service.TenantThemeService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantThemeServiceImpl implements TenantThemeService {

    private final TenantThemeRepository tenantThemeRepository;
    private final TenantRepository tenantRepository;

    @Override
    public TenantThemeResponse getTheme(Long tenantId) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return tenantThemeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .map(this::mapToResponse)
                .orElse(buildDefaultResponse(tenant));
    }

    @Override
    public TenantThemeResponse getMyTheme() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return tenantThemeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .map(this::mapToResponse)
                .orElse(buildDefaultResponse(tenant));
    }

    @Override
    @Transactional
    public TenantThemeResponse updateTheme(Long tenantId, TenantThemeRequest request) {
        String currentRole = SecurityUtil.getCurrentRole();
        if (!"SUPER_ADMIN".equals(currentRole)) {
            Long currentTenantId = SecurityUtil.getCurrentTenantId();
            if (!currentTenantId.equals(tenantId)) {
                throw new FerosException("Access denied: cannot update another tenant's theme", HttpStatus.FORBIDDEN);
            }
        }

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        TenantTheme theme = tenantThemeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .orElse(TenantTheme.builder()
                        .tenant(tenant)
                        .isActive(true)
                        .build());

        if (request.getLogoUrl() != null)
            theme.setLogoUrl(request.getLogoUrl());
        if (request.getFaviconUrl() != null)
            theme.setFaviconUrl(request.getFaviconUrl());
        if (request.getPrimaryColor() != null)
            theme.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null)
            theme.setSecondaryColor(request.getSecondaryColor());
        if (request.getAccentColor() != null)
            theme.setAccentColor(request.getAccentColor());
        if (request.getSidebarColor() != null)
            theme.setSidebarColor(request.getSidebarColor());
        if (request.getTextColor() != null)
            theme.setTextColor(request.getTextColor());
        if (request.getFontFamily() != null)
            theme.setFontFamily(request.getFontFamily());
        if (request.getCompanyTagline() != null)
            theme.setCompanyTagline(request.getCompanyTagline());

        return mapToResponse(tenantThemeRepository.save(theme));
    }

    private TenantThemeResponse mapToResponse(TenantTheme t) {
        return TenantThemeResponse.builder()
                .id(t.getId())
                .tenantId(t.getTenant().getId())
                .tenantName(t.getTenant().getCompanyName())
                .logoUrl(t.getLogoUrl())
                .faviconUrl(t.getFaviconUrl())
                .primaryColor(t.getPrimaryColor())
                .secondaryColor(t.getSecondaryColor())
                .accentColor(t.getAccentColor())
                .sidebarColor(t.getSidebarColor())
                .textColor(t.getTextColor())
                .fontFamily(t.getFontFamily())
                .companyTagline(t.getCompanyTagline())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TenantThemeResponse buildDefaultResponse(Tenant tenant) {
        return TenantThemeResponse.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getCompanyName())
                .primaryColor("#1E40AF")
                .secondaryColor("#64748B")
                .accentColor("#F59E0B")
                .sidebarColor("#1E293B")
                .textColor("#0F172A")
                .fontFamily("Inter")
                .isActive(true)
                .build();
    }
}