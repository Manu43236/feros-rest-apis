package com.feros.api.controller;

import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.VehicleServiceResponse;
import com.feros.api.service.VehicleMaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle-services")
@RequiredArgsConstructor
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService vehicleMaintenanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle services fetched successfully", vehicleMaintenanceService.getAll()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle services fetched successfully", vehicleMaintenanceService.getByVehicle(vehicleId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle service fetched successfully", vehicleMaintenanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> create(
            @Valid @RequestBody VehicleServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle service recorded successfully", vehicleMaintenanceService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody VehicleServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle service updated successfully", vehicleMaintenanceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        vehicleMaintenanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle service deleted successfully", null));
    }
}
