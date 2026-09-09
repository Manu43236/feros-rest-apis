package com.feros.api.controller;

import com.feros.api.dto.request.GpsDeviceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.GpsDeviceResponse;
import com.feros.api.service.GpsDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gps/devices")
@RequiredArgsConstructor
public class GpsDeviceController {

    private final GpsDeviceService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<GpsDeviceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("GPS devices fetched", service.getAll()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<GpsDeviceResponse>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("GPS device fetched", service.getByVehicle(vehicleId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceResponse>> register(
            @Valid @RequestBody GpsDeviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS device registered", service.register(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody GpsDeviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GPS device updated", service.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GpsDeviceResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("GPS device deactivated", service.deactivate(id)));
    }
}
