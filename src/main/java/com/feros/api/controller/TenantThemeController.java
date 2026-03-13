package com.feros.api.controller;

import com.feros.api.dto.request.TenantThemeRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TenantThemeResponse;
import com.feros.api.service.TenantThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/themes")
@RequiredArgsConstructor
public class TenantThemeController {

    private final TenantThemeService tenantThemeService;

    // Any authenticated user can fetch their own theme (used by frontend on login)
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantThemeResponse>> getMyTheme() {
        return ResponseEntity.ok(ApiResponse.success(
                "Theme fetched successfully", tenantThemeService.getMyTheme()));
    }

    // SUPER_ADMIN can fetch any tenant's theme; ADMIN can fetch their own
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantThemeResponse>> getTheme(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Theme fetched successfully", tenantThemeService.getTheme(tenantId)));
    }

    // ADMIN can update their own theme, SUPER_ADMIN can update any
    @PutMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantThemeResponse>> updateTheme(
            @PathVariable Long tenantId,
            @RequestBody TenantThemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Theme updated successfully",
                tenantThemeService.updateTheme(tenantId, request)));
    }
}