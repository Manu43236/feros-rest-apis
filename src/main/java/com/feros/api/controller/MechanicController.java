package com.feros.api.controller;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.MechanicVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;
import com.feros.api.service.InventoryService;
import com.feros.api.service.MechanicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/mechanic")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;
    private final InventoryService inventoryService;

    /** Returns all vehicles/services with tasks assigned to the logged-in mechanic. */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<ApiResponse<List<MechanicVehicleTasksResponse>>> getMyTasks() {
        return ResponseEntity.ok(ApiResponse.success(
                "Tasks fetched successfully", mechanicService.getMyTasks()));
    }

    /** Mechanic starts a task (ASSIGNED → IN_PROGRESS). */
    @PutMapping("/tasks/{taskId}/start")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<ApiResponse<MechanicVehicleTasksResponse>> startTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Task started", mechanicService.startTask(taskId)));
    }

    /** Mechanic closes their task (IN_PROGRESS / ASSIGNED → MECHANIC_CLOSED). */
    @PutMapping("/tasks/{taskId}/close")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<ApiResponse<MechanicVehicleTasksResponse>> closeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Task closed successfully", mechanicService.closeTask(taskId)));
    }

    /** Mechanic requests a spare part for a task (delegates to the task's service). */
    @PostMapping("/tasks/{taskId}/spare-part-request")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<ApiResponse<ServicePartResponse>> requestSparePart(
            @PathVariable Long taskId,
            @Valid @RequestBody ServicePartRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Spare part request submitted", mechanicService.requestSparePart(taskId, request)));
    }

    /** Mechanic views all spare part requests for a specific task. */
    @GetMapping("/tasks/{taskId}/spare-part-requests")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<ApiResponse<List<ServicePartResponse>>> getSparePartRequests(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Spare part requests fetched", inventoryService.getPartsForTask(taskId)));
    }
}
