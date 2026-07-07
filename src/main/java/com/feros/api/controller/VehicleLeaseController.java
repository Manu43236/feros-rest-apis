package com.feros.api.controller;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.LeaseSessionStartRequest;
import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
import com.feros.api.dto.request.VehicleLeaseRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.LeaseBillingResponse;
import com.feros.api.dto.response.LeaseVehicleAssignmentResponse;
import com.feros.api.dto.response.LeaseVehicleSessionResponse;
import com.feros.api.dto.response.VehicleLeaseResponse;
import com.feros.api.enums.LeaseStatus;
import com.feros.api.service.VehicleLeaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle-leases")
@RequiredArgsConstructor
public class VehicleLeaseController {

    private final VehicleLeaseService vehicleLeaseService;

    // ── Leases ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<VehicleLeaseResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LeaseStatus status,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success("Leases fetched",
                vehicleLeaseService.getAll(page, size, status, clientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<VehicleLeaseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lease fetched", vehicleLeaseService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleLeaseResponse>> create(@Valid @RequestBody VehicleLeaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lease created", vehicleLeaseService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleLeaseResponse>> update(
            @PathVariable Long id, @Valid @RequestBody VehicleLeaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lease updated", vehicleLeaseService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleLeaseResponse>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        LeaseStatus newStatus = LeaseStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated", vehicleLeaseService.updateStatus(id, newStatus)));
    }

    @PutMapping("/{id}/extend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleLeaseResponse>> extend(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate newEndDate = LocalDate.parse(body.get("newEndDate"));
        return ResponseEntity.ok(ApiResponse.success("Lease extended", vehicleLeaseService.extend(id, newEndDate)));
    }

    // ── Vehicles ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<LeaseVehicleAssignmentResponse>>> getVehicles(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lease vehicles fetched", vehicleLeaseService.getVehicles(id)));
    }

    @PostMapping("/{id}/vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LeaseVehicleAssignmentResponse>> addVehicle(
            @PathVariable Long id, @Valid @RequestBody LeaseVehicleAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle added to lease", vehicleLeaseService.addVehicle(id, request)));
    }

    @PutMapping("/{id}/vehicles/{assignmentId}/division")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LeaseVehicleAssignmentResponse>> assignDivision(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody AssignDivisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Division assigned",
                vehicleLeaseService.assignDivision(id, assignmentId, request)));
    }

    @PutMapping("/{id}/vehicles/{assignmentId}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LeaseVehicleAssignmentResponse>> closeVehicle(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> body) {
        BigDecimal odometer = body.containsKey("odometerAtEnd")
                ? new BigDecimal(body.get("odometerAtEnd")) : null;
        return ResponseEntity.ok(ApiResponse.success("Vehicle assignment closed",
                vehicleLeaseService.closeVehicleAssignment(id, assignmentId, odometer)));
    }

    // ── Billing ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/billing")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<LeaseBillingResponse>> getBilling(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Billing fetched", vehicleLeaseService.getBilling(id)));
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/vehicles/{assignmentId}/sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<LeaseVehicleSessionResponse>> startSession(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @Valid @RequestBody LeaseSessionStartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session started",
                vehicleLeaseService.startSession(id, assignmentId, request)));
    }

    @PutMapping("/{id}/vehicles/{assignmentId}/sessions/end")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<LeaseVehicleSessionResponse>> endSession(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody(required = false) Map<String, String> body) {
        LocalDateTime endTime = (body != null && body.containsKey("endTime"))
                ? LocalDateTime.parse(body.get("endTime")) : null;
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(ApiResponse.success("Session ended",
                vehicleLeaseService.endSession(id, assignmentId, endTime, notes)));
    }

    @GetMapping("/{id}/sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<LeaseVehicleSessionResponse>>> getSessions(
            @PathVariable Long id,
            @RequestParam(required = false) Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched",
                vehicleLeaseService.getSessions(id, assignmentId)));
    }
}
