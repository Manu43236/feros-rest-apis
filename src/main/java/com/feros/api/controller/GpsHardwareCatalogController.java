package com.feros.api.controller;

import com.feros.api.dto.request.GpsDeviceModelRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.GpsDeviceModelResponse;
import com.feros.api.service.GpsHardwareCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sa/gps")
@RequiredArgsConstructor
public class GpsHardwareCatalogController {

    private final GpsHardwareCatalogService service;

    @GetMapping("/models")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<GpsDeviceModelResponse>>> getAll(
            @RequestParam(required = false) String company) {
        return ResponseEntity.ok(ApiResponse.success("GPS device models fetched", service.getAll(company)));
    }

    @GetMapping("/models/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GpsDeviceModelResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success("Active GPS device models fetched", service.getAllActive()));
    }

    @GetMapping("/models/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceModelResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("GPS device model fetched", service.getById(id)));
    }

    @PostMapping("/models")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceModelResponse>> create(
            @Valid @RequestBody GpsDeviceModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS device model created", service.create(request)));
    }

    @PutMapping("/models/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceModelResponse>> update(
            @PathVariable Long id, @Valid @RequestBody GpsDeviceModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS device model updated", service.update(id, request)));
    }

    @PatchMapping("/models/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceModelResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("GPS device model status toggled", service.toggleActive(id)));
    }
}
