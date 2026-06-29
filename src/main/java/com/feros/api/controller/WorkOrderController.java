package com.feros.api.controller;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.DailyLogRequest;
import com.feros.api.dto.request.MachineAssignmentRequest;
import com.feros.api.dto.request.WorkOrderRequest;
import com.feros.api.dto.response.*;
import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.WorkOrderStatus;
import com.feros.api.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    // ── Work Orders ───────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<WorkOrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success("Work orders fetched",
                workOrderService.getAll(page, size, status, clientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<WorkOrderDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Work order fetched", workOrderService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> create(@Valid @RequestBody WorkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work order created", workOrderService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> update(
            @PathVariable Long id, @Valid @RequestBody WorkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work order updated", workOrderService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        WorkOrderStatus newStatus = WorkOrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated", workOrderService.updateStatus(id, newStatus)));
    }

    @PutMapping("/{id}/extend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> extend(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate newEndDate = LocalDate.parse(body.get("newEndDate"));
        return ResponseEntity.ok(ApiResponse.success("Work order extended", workOrderService.extend(id, newEndDate)));
    }

    // ── Machine Assignments ───────────────────────────────────────────────────

    @PostMapping("/{id}/machines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<MachineAssignmentResponse>> addMachine(
            @PathVariable Long id, @Valid @RequestBody MachineAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Machine assigned", workOrderService.addMachine(id, request)));
    }

    @PutMapping("/{id}/machines/{assignmentId}/operator")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<MachineAssignmentResponse>> assignOperator(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody com.feros.api.dto.request.AssignOperatorRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Operator assigned", workOrderService.assignOperator(id, assignmentId, request)));
    }

    @PostMapping("/{id}/machines/{assignmentId}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<com.feros.api.dto.response.WorkEntryResponse>> startWork(
            @PathVariable Long id, @PathVariable Long assignmentId,
            @Valid @RequestBody com.feros.api.dto.request.StartWorkEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work started", workOrderService.startWork(id, assignmentId, request)));
    }

    @PutMapping("/{id}/machines/{assignmentId}/stop")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<com.feros.api.dto.response.WorkEntryResponse>> stopWork(
            @PathVariable Long id, @PathVariable Long assignmentId,
            @Valid @RequestBody com.feros.api.dto.request.StopWorkEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work stopped", workOrderService.stopWork(id, assignmentId, request)));
    }

    @GetMapping("/{id}/work-entries")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<com.feros.api.dto.response.WorkEntryResponse>>> getAllWorkEntries(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Work entries fetched", workOrderService.getAllWorkEntries(id)));
    }

    @GetMapping("/{id}/machines/{assignmentId}/work-entries")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<com.feros.api.dto.response.WorkEntryResponse>>> getWorkEntries(
            @PathVariable Long id, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success("Work entries fetched", workOrderService.getWorkEntries(id, assignmentId)));
    }

    @PutMapping("/{id}/machines/{assignmentId}/division")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<MachineAssignmentResponse>> assignDivision(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody AssignDivisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Division assigned", workOrderService.assignDivision(id, assignmentId, request)));
    }

    @PutMapping("/{id}/machines/{assignmentId}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> closeMachine(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> body) {
        LocalDate endDate = body.containsKey("endDate") ? LocalDate.parse(body.get("endDate")) : null;
        AssignmentEndReason reason = AssignmentEndReason.valueOf(body.get("endReason"));
        workOrderService.closeMachineAssignment(id, assignmentId, endDate, reason);
        return ResponseEntity.ok(ApiResponse.success("Machine assignment closed", null));
    }

    // ── Daily Logs ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DailyLogResponse>> addLog(
            @PathVariable Long id, @Valid @RequestBody DailyLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Log added", workOrderService.addLog(id, request)));
    }

    @PutMapping("/{id}/logs/{logId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DailyLogResponse>> updateLog(
            @PathVariable Long id,
            @PathVariable Long logId,
            @Valid @RequestBody DailyLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Log updated", workOrderService.updateLog(id, logId, request)));
    }

    @DeleteMapping("/{id}/logs/{logId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLog(
            @PathVariable Long id, @PathVariable Long logId) {
        workOrderService.deleteLog(id, logId);
        return ResponseEntity.ok(ApiResponse.success("Log deleted", null));
    }
}
