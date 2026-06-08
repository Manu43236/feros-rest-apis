package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.MechanicSummaryResponse;
import com.feros.api.dto.response.ServiceManagerDashboardResponse;
import com.feros.api.service.ServiceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-manager")
@RequiredArgsConstructor
public class ServiceManagerController {

    private final ServiceManagerService serviceManagerService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<ServiceManagerDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                "Service manager dashboard fetched successfully",
                serviceManagerService.getDashboard()));
    }

    @GetMapping("/mechanics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<MechanicSummaryResponse>>> getMechanics() {
        return ResponseEntity.ok(ApiResponse.success(
                "Mechanics fetched successfully",
                serviceManagerService.getMechanics()));
    }
}
