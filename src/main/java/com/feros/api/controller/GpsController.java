package com.feros.api.controller;

import com.feros.api.dto.request.GpsProviderConfigRequest;
import com.feros.api.dto.request.VehicleGpsMappingRequest;
import com.feros.api.dto.response.*;
import com.feros.api.service.GpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gps")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService gpsService;

    // ─── Provider configs ────────────────────────────────────────────────────────

    @GetMapping("/configs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GpsProviderConfigResponse>>> getAllConfigs() {
        return ResponseEntity.ok(ApiResponse.success("GPS configs fetched", gpsService.getAllConfigs()));
    }

    @PostMapping("/configs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<GpsProviderConfigResponse>> createConfig(
            @Valid @RequestBody GpsProviderConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS config created", gpsService.createConfig(request)));
    }

    @PutMapping("/configs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<GpsProviderConfigResponse>> updateConfig(
            @PathVariable Long id, @Valid @RequestBody GpsProviderConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS config updated", gpsService.updateConfig(id, request)));
    }

    @DeleteMapping("/configs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        gpsService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success("GPS config deleted", null));
    }

    @PostMapping("/configs/{id}/test")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> testConnection(@PathVariable Long id) {
        boolean success = gpsService.testConnection(id);
        String message = success ? "Connection successful" : "Connection failed";
        return ResponseEntity.ok(ApiResponse.success(message, success));
    }

    @GetMapping("/configs/{id}/provider-vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GpsProviderVehicleResponse>>> getProviderVehicles(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Provider vehicles fetched", gpsService.getProviderVehicles(id)));
    }

    // ─── Vehicle mappings ─────────────────────────────────────────────────────────

    @GetMapping("/mappings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleGpsMappingResponse>>> getAllMappings() {
        return ResponseEntity.ok(ApiResponse.success("GPS mappings fetched", gpsService.getAllMappings()));
    }

    @PostMapping("/mappings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleGpsMappingResponse>> createMapping(
            @Valid @RequestBody VehicleGpsMappingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle mapped to GPS", gpsService.createMapping(request)));
    }

    @DeleteMapping("/mappings/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(@PathVariable Long id) {
        gpsService.deleteMapping(id);
        return ResponseEntity.ok(ApiResponse.success("GPS mapping removed", null));
    }

    // ─── Fleet map ────────────────────────────────────────────────────────────────

    @GetMapping("/fleet")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<GpsFleetVehicleResponse>>> getFleet() {
        return ResponseEntity.ok(ApiResponse.success("Fleet locations fetched", gpsService.getFleet()));
    }
}
