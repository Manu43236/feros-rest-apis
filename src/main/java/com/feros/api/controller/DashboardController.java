package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.ExpiryAlertResponse;
import com.feros.api.service.DashboardService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER', 'SERVICE_MEN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<Object>> getDashboard() {
        String role = SecurityUtil.getCurrentRole();
        Object data = switch (role) {
            case "SUPERVISOR"              -> dashboardService.getSupervisorDashboard();
            case "DRIVER", "CLEANER",
                 "SERVICE_MEN", "STORE_KEEPER" -> dashboardService.getDriverDashboard();
            default                        -> dashboardService.getDashboard(); // ADMIN, OFFICE_STAFF
        };
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched successfully", data));
    }

    @GetMapping("/expiry-alerts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ExpiryAlertResponse>> getExpiryAlerts(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(
                "Expiry alerts fetched successfully", dashboardService.getExpiryAlerts(days)));
    }
}
