package com.feros.api.controller;

import com.feros.api.dto.request.RoleModuleAccessRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.RoleModuleAccessResponse;
import com.feros.api.service.RoleModuleAccessService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/role-module-access")
@RequiredArgsConstructor
public class RoleModuleAccessController {

    private final RoleModuleAccessService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleModuleAccessResponse>> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Module access retrieved", service.getAll(tenantId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> saveAll(@RequestBody RoleModuleAccessRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        service.saveAll(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Module access saved", null));
    }
}
