package com.feros.api.controller;

import com.feros.api.dto.request.CompleteServiceRequest;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle-services")
@RequiredArgsConstructor
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService vehicleMaintenanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle services fetched successfully", vehicleMaintenanceService.getAll()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle services fetched successfully", vehicleMaintenanceService.getByVehicle(vehicleId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle service fetched successfully", vehicleMaintenanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> create(@Valid @RequestBody VehicleServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service created successfully", vehicleMaintenanceService.create(request)));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> start(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service started successfully", vehicleMaintenanceService.start(id)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service undone to Open", vehicleMaintenanceService.cancel(id)));
    }

    @PutMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> updateNotes(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Notes updated", vehicleMaintenanceService.updateNotes(id, body.get("notes"))));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> complete(
            @PathVariable Long id,
            @Valid @RequestBody CompleteServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service completed successfully", vehicleMaintenanceService.complete(id, request)));
    }

    @PatchMapping("/{serviceId}/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> completeTask(
            @PathVariable Long serviceId, @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task marked complete", vehicleMaintenanceService.completeTask(serviceId, taskId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        vehicleMaintenanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }
}
