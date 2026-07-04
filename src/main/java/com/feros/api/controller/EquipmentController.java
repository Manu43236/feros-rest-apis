package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentFuelLogRequest;
import com.feros.api.dto.request.EquipmentServiceRequest;
import com.feros.api.dto.request.EquipmentMeterReadingRequest;
import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentDashboardResponse;
import com.feros.api.dto.response.EquipmentFuelLogResponse;
import com.feros.api.dto.response.EquipmentServiceResponse;
import com.feros.api.dto.response.EquipmentMeterReadingResponse;
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

    // ── Fuel Logs ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/fuel-logs")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentFuelLogResponse>>> getFuelLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fuel logs fetched", equipmentService.getFuelLogs(id)));
    }

    @PostMapping("/{id}/fuel-logs")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentFuelLogResponse>> addFuelLog(
            @PathVariable Long id, @RequestBody EquipmentFuelLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel log added", equipmentService.addFuelLog(id, request)));
    }

    @PutMapping("/{id}/fuel-logs/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentFuelLogResponse>> updateFuelLog(
            @PathVariable Long id, @PathVariable Long logId, @RequestBody EquipmentFuelLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel log updated", equipmentService.updateFuelLog(id, logId, request)));
    }

    @DeleteMapping("/{id}/fuel-logs/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteFuelLog(@PathVariable Long id, @PathVariable Long logId) {
        equipmentService.deleteFuelLog(id, logId);
        return ResponseEntity.ok(ApiResponse.success("Fuel log deleted", null));
    }

    // ── Meter Readings ────────────────────────────────────────────────────────

    @GetMapping("/{id}/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentMeterReadingResponse>>> getMeterReadings(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Meter readings fetched", equipmentService.getMeterReadings(id)));
    }

    @PostMapping("/{id}/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentMeterReadingResponse>> addMeterReading(
            @PathVariable Long id, @RequestBody EquipmentMeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meter reading added", equipmentService.addMeterReading(id, request)));
    }

    @PutMapping("/{id}/meter-readings/{readingId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentMeterReadingResponse>> updateMeterReading(
            @PathVariable Long id, @PathVariable Long readingId, @RequestBody EquipmentMeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meter reading updated", equipmentService.updateMeterReading(id, readingId, request)));
    }

    @DeleteMapping("/{id}/meter-readings/{readingId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteMeterReading(@PathVariable Long id, @PathVariable Long readingId) {
        equipmentService.deleteMeterReading(id, readingId);
        return ResponseEntity.ok(ApiResponse.success("Meter reading deleted", null));
    }
    // ── Service Records ───────────────────────────────────────────────────────

    @GetMapping("/{id}/services")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentServiceResponse>>> getServices(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Services fetched", equipmentService.getServices(id)));
    }

    @PostMapping("/{id}/services")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentServiceResponse>> createService(
            @PathVariable Long id, @Valid @RequestBody EquipmentServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service created", equipmentService.createService(id, request)));
    }

    @PutMapping("/{id}/services/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentServiceResponse>> updateService(
            @PathVariable Long id, @PathVariable Long serviceId, @Valid @RequestBody EquipmentServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service updated", equipmentService.updateService(id, serviceId, request)));
    }

    @PostMapping("/{id}/services/{serviceId}/start")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentServiceResponse>> startService(
            @PathVariable Long id, @PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success("Service started", equipmentService.startService(id, serviceId)));
    }

    @PostMapping("/{id}/services/{serviceId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentServiceResponse>> completeService(
            @PathVariable Long id, @PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success("Service completed", equipmentService.completeService(id, serviceId)));
    }

    @DeleteMapping("/{id}/services/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id, @PathVariable Long serviceId) {
        equipmentService.deleteService(id, serviceId);
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }

}