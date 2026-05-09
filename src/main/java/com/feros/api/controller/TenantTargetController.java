package com.feros.api.controller;

import com.feros.api.dto.request.TenantTargetRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TenantTargetResponse;
import com.feros.api.service.TenantTargetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/targets")
@RequiredArgsConstructor
public class TenantTargetController {

    private final TenantTargetService tenantTargetService;

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<TenantTargetResponse>> setTarget(
            @Valid @RequestBody TenantTargetRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Target saved", tenantTargetService.setTarget(request)));
    }

    @GetMapping("/{year}/{month}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantTargetResponse>> getTarget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return ResponseEntity.ok(ApiResponse.success("Target fetched", tenantTargetService.getTarget(year, month)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TenantTargetResponse>>> getAllTargets() {
        return ResponseEntity.ok(ApiResponse.success("Targets fetched", tenantTargetService.getAllTargets()));
    }
}
