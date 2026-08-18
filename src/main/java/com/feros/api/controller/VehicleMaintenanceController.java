package com.feros.api.controller;

import com.feros.api.dto.request.AssignMechanicRequest;
import com.feros.api.dto.request.CompleteServiceRequest;
import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.request.VehicleServiceTaskRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.ServiceVendorItemResponse;
import com.feros.api.dto.response.VehicleServiceResponse;
import com.feros.api.service.VehicleMaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle-services")
@RequiredArgsConstructor
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService vehicleMaintenanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle services fetched successfully", vehicleMaintenanceService.getAll()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<VehicleServiceResponse>>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle services fetched successfully", vehicleMaintenanceService.getByVehicle(vehicleId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle service fetched successfully", vehicleMaintenanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> create(@Valid @RequestBody VehicleServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service created successfully", vehicleMaintenanceService.create(request)));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> complete(
            @PathVariable Long id,
            @Valid @RequestBody CompleteServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service completed successfully", vehicleMaintenanceService.complete(id, request)));
    }

    @PatchMapping("/{serviceId}/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> completeTask(
            @PathVariable Long serviceId, @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task marked complete", vehicleMaintenanceService.completeTask(serviceId, taskId)));
    }

    @PutMapping("/{serviceId}/tasks/{taskId}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> assignTask(
            @PathVariable Long serviceId,
            @PathVariable Long taskId,
            @Valid @RequestBody AssignMechanicRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Technician assigned successfully",
                vehicleMaintenanceService.assignTask(serviceId, taskId, request.getMechanicId())));
    }

    @PostMapping("/{id}/tasks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> addTask(
            @PathVariable Long id,
            @RequestBody VehicleServiceTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task added successfully", vehicleMaintenanceService.addTask(id, request)));
    }

    @PutMapping("/{id}/charges")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> updateCharges(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal charges = body.get("estimatedCost") != null
                ? new BigDecimal(body.get("estimatedCost").toString()) : null;
        return ResponseEntity.ok(ApiResponse.success("Estimated cost updated", vehicleMaintenanceService.updateEstimatedCost(id, charges)));
    }

    @PostMapping("/{id}/estimate-doc")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> uploadEstimateDoc(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Estimate document uploaded", vehicleMaintenanceService.uploadEstimateDoc(id, file)));
    }

    @PostMapping("/{id}/bill-doc")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleServiceResponse>> uploadBillDoc(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Bill document uploaded", vehicleMaintenanceService.uploadBillDoc(id, file)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        vehicleMaintenanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }

    @PostMapping("/{id}/vendor-items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ServiceVendorItemResponse>> addVendorItem(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        String description = (String) body.get("description");
        BigDecimal cost = body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null;
        return ResponseEntity.ok(ApiResponse.success("Item added", vehicleMaintenanceService.addVendorItem(id, description, cost)));
    }

    @DeleteMapping("/{id}/vendor-items/{itemId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVendorItem(
            @PathVariable Long id, @PathVariable Long itemId) {
        vehicleMaintenanceService.deleteVendorItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item deleted", null));
    }
}
