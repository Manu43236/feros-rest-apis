package com.feros.api.controller;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TechnicianVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;
import com.feros.api.service.InventoryService;
import com.feros.api.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/technician")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;
    private final InventoryService inventoryService;

    /** Returns all vehicles/services with tasks assigned to the logged-in technician. */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<TechnicianVehicleTasksResponse>>> getMyTasks() {
        return ResponseEntity.ok(ApiResponse.success(
                "Tasks fetched successfully", technicianService.getMyTasks()));
    }

    /** Technician starts a task (ASSIGNED → IN_PROGRESS). */
    @PutMapping("/tasks/{taskId}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<TechnicianVehicleTasksResponse>> startTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Task started", technicianService.startTask(taskId)));
    }

    /** Technician closes their task (IN_PROGRESS / ASSIGNED → MECHANIC_CLOSED). */
    @PutMapping("/tasks/{taskId}/close")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<TechnicianVehicleTasksResponse>> closeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Task closed successfully", technicianService.closeTask(taskId)));
    }

    /** Technician requests a spare part for a task. */
    @PostMapping("/tasks/{taskId}/spare-part-request")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<ServicePartResponse>> requestSparePart(
            @PathVariable Long taskId,
            @Valid @RequestBody ServicePartRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Spare part request submitted", technicianService.requestSparePart(taskId, request)));
    }

    /** Technician views all spare part requests for a specific task. */
    @GetMapping("/tasks/{taskId}/spare-part-requests")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<ServicePartResponse>>> getSparePartRequests(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Spare part requests fetched", inventoryService.getPartsForTask(taskId)));
    }
}
