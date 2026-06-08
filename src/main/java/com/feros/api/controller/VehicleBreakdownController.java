package com.feros.api.controller;

import com.feros.api.dto.request.BreakdownRequest;
import com.feros.api.dto.request.BreakdownReplaceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BreakdownResponse;
import com.feros.api.service.VehicleBreakdownService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VehicleBreakdownController {

    private final VehicleBreakdownService breakdownService;

    // Admin / Supervisor / OfficeStaff — report breakdown by order + allocation
    @PostMapping("/orders/{orderId}/vehicle-allocations/{allocationId}/breakdown")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> reportBreakdown(
            @PathVariable Long orderId,
            @PathVariable Long allocationId,
            @Valid @RequestBody BreakdownRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown reported", breakdownService.reportBreakdown(orderId, allocationId, request)));
    }

    // Driver — reports their own active trip breakdown from mobile
    @PostMapping("/my/breakdown")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> reportMyBreakdown(
            @Valid @RequestBody BreakdownRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown reported", breakdownService.reportMyBreakdown(request)));
    }

    // Get breakdown record for a specific allocation
    @GetMapping("/orders/{orderId}/vehicle-allocations/{allocationId}/breakdown")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR','DRIVER')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> getBreakdown(
            @PathVariable Long orderId,
            @PathVariable Long allocationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown fetched", breakdownService.getBreakdownByAllocation(orderId, allocationId)));
    }

    // Assign replacement vehicle
    @PostMapping("/orders/{orderId}/breakdowns/{breakdownId}/replace")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> replaceVehicle(
            @PathVariable Long orderId,
            @PathVariable Long breakdownId,
            @Valid @RequestBody BreakdownReplaceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Replacement vehicle assigned", breakdownService.replaceVehicle(orderId, breakdownId, request)));
    }

    // Vehicle repaired on road — trip continues
    @PostMapping("/orders/{orderId}/breakdowns/{breakdownId}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> resolveBreakdown(
            @PathVariable Long orderId,
            @PathVariable Long breakdownId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown resolved — trip continues", breakdownService.resolveBreakdown(orderId, breakdownId)));
    }

    // Cancel false alarm — only when REPORTED and no replacement assigned
    @DeleteMapping("/orders/{orderId}/breakdowns/{breakdownId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> cancelBreakdown(
            @PathVariable Long orderId,
            @PathVariable Long breakdownId) {
        breakdownService.cancelBreakdown(orderId, breakdownId);
        return ResponseEntity.ok(ApiResponse.success("Breakdown report cancelled", null));
    }

    // Vehicle breakdown history — for maintenance tracking
    @GetMapping("/vehicle-breakdowns")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<BreakdownResponse>>> getVehicleBreakdownHistory(
            @RequestParam(required = false) Long vehicleId) {
        List<BreakdownResponse> data = (vehicleId != null)
                ? breakdownService.getBreakdownHistoryByVehicle(vehicleId)
                : breakdownService.getAllBreakdowns();
        return ResponseEntity.ok(ApiResponse.success("Breakdown history fetched", data));
    }

    // Standalone — mark available vehicle as BREAKDOWN (not on any order)
    @PostMapping("/vehicles/{vehicleId}/breakdown")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> reportStandaloneBreakdown(
            @PathVariable Long vehicleId,
            @Valid @RequestBody BreakdownRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown reported", breakdownService.reportStandaloneBreakdown(vehicleId, request)));
    }

    // Standalone — resolve breakdown and mark vehicle back to AVAILABLE
    @PostMapping("/vehicles/{vehicleId}/breakdown/{breakdownId}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SUPERVISOR','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<BreakdownResponse>> resolveStandaloneBreakdown(
            @PathVariable Long vehicleId,
            @PathVariable Long breakdownId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdown resolved — vehicle is now available",
                breakdownService.resolveStandaloneBreakdown(vehicleId, breakdownId)));
    }
}
