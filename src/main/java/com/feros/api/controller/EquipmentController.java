package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentDashboardResponse;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.dto.response.MachineAssignmentHistoryResponse;
import com.feros.api.dto.response.MachineInvoiceItemResponse;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched", equipmentService.getDashboard()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Equipment fetched successfully",
                equipmentService.getAllEquipment()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Equipment fetched successfully",
                equipmentService.getEquipmentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> create(@Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment created successfully",
                equipmentService.createEquipment(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> update(
            @PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment updated successfully",
                equipmentService.updateEquipment(id, request)));
    }

    @PatchMapping("/{id}/work-status")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateWorkStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        EquipmentWorkStatus status = EquipmentWorkStatus.valueOf(body.get("workStatus"));
        return ResponseEntity.ok(ApiResponse.success("Work status updated successfully",
                equipmentService.updateWorkStatus(id, status)));
    }

    // ── Machine Detail ───────────────────────────────────────────────────────

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<MachineAssignmentHistoryResponse>>> getMachineHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Assignment history fetched",
                equipmentService.getMachineAssignmentHistory(id)));
    }

    @GetMapping("/{id}/daily-logs")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> getMachineLogs(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Daily logs fetched",
                equipmentService.getMachineDailyLogs(id, from, to)));
    }

    @GetMapping("/{id}/invoice-items")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<MachineInvoiceItemResponse>>> getMachineInvoiceItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice items fetched",
                equipmentService.getMachineInvoiceItems(id)));
    }
}
