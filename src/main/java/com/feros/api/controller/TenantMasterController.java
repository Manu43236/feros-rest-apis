package com.feros.api.controller;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.HolidayResponse;
import com.feros.api.dto.response.TenantMasterResponse;
import com.feros.api.dto.request.RbacLoginAccessRequest;
import com.feros.api.dto.response.RbacLoginAccessResponse;
import com.feros.api.service.TenantMasterService;
import com.feros.api.util.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/masters/tenant")
@RequiredArgsConstructor
public class TenantMasterController {

    private final TenantMasterService tenantMasterService;

    // ===================== VEHICLE STATUSES =====================
    @GetMapping("/vehicle-statuses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllVehicleStatuses() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle statuses fetched successfully",
                tenantMasterService.getAllVehicleStatuses()));
    }

    @GetMapping("/vehicle-statuses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getVehicleStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle status fetched successfully",
                tenantMasterService.getVehicleStatusById(id)));
    }

    @PostMapping("/vehicle-statuses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createVehicleStatus(
            @Valid @RequestBody VehicleStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle status created successfully",
                tenantMasterService.createVehicleStatus(request)));
    }

    @PutMapping("/vehicle-statuses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updateVehicleStatus(@PathVariable Long id,
            @Valid @RequestBody VehicleStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle status updated successfully",
                tenantMasterService.updateVehicleStatus(id, request)));
    }

    @DeleteMapping("/vehicle-statuses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleStatus(@PathVariable Long id) {
        tenantMasterService.deleteVehicleStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle status deleted successfully", null));
    }

    // ===================== ROUTES =====================
    @GetMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllRoutes() {
        return ResponseEntity
                .ok(ApiResponse.success("Routes fetched successfully", tenantMasterService.getAllRoutes()));
    }

    @GetMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getRouteById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Route fetched successfully", tenantMasterService.getRouteById(id)));
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createRoute(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success("Route created successfully", tenantMasterService.createRoute(request)));
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updateRoute(@PathVariable Long id,
            @Valid @RequestBody RouteRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success("Route updated successfully", tenantMasterService.updateRoute(id, request)));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        tenantMasterService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted successfully", null));
    }

    // ===================== DESIGNATIONS =====================
    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllDesignations() {
        return ResponseEntity
                .ok(ApiResponse.success("Designations fetched successfully", tenantMasterService.getAllDesignations()));
    }

    @GetMapping("/designations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getDesignationById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Designation fetched successfully", tenantMasterService.getDesignationById(id)));
    }

    @PostMapping("/designations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createDesignation(
            @Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Designation created successfully",
                tenantMasterService.createDesignation(request)));
    }

    @PutMapping("/designations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updateDesignation(@PathVariable Long id,
            @Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Designation updated successfully",
                tenantMasterService.updateDesignation(id, request)));
    }

    @DeleteMapping("/designations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        tenantMasterService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deleted successfully", null));
    }

    // ===================== CHARGE TYPES =====================
    @GetMapping("/charge-types")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllChargeTypes() {
        return ResponseEntity
                .ok(ApiResponse.success("Charge types fetched successfully", tenantMasterService.getAllChargeTypes()));
    }

    @GetMapping("/charge-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getChargeTypeById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Charge type fetched successfully", tenantMasterService.getChargeTypeById(id)));
    }

    @PostMapping("/charge-types")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createChargeType(
            @Valid @RequestBody ChargeTypeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Charge type created successfully", tenantMasterService.createChargeType(request)));
    }

    @PutMapping("/charge-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updateChargeType(@PathVariable Long id,
            @Valid @RequestBody ChargeTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Charge type updated successfully",
                tenantMasterService.updateChargeType(id, request)));
    }

    @DeleteMapping("/charge-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteChargeType(@PathVariable Long id) {
        tenantMasterService.deleteChargeType(id);
        return ResponseEntity.ok(ApiResponse.success("Charge type deleted successfully", null));
    }

    // ===================== PAYMENT TERMS =====================
    @GetMapping("/payment-terms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllPaymentTerms() {
        return ResponseEntity.ok(
                ApiResponse.success("Payment terms fetched successfully", tenantMasterService.getAllPaymentTerms()));
    }

    @GetMapping("/payment-terms/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getPaymentTermsById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment terms fetched successfully", tenantMasterService.getPaymentTermsById(id)));
    }

    @PostMapping("/payment-terms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createPaymentTerms(
            @Valid @RequestBody PaymentTermsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment terms created successfully",
                tenantMasterService.createPaymentTerms(request)));
    }

    @PutMapping("/payment-terms/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updatePaymentTerms(@PathVariable Long id,
            @Valid @RequestBody PaymentTermsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment terms updated successfully",
                tenantMasterService.updatePaymentTerms(id, request)));
    }

    @DeleteMapping("/payment-terms/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentTerms(@PathVariable Long id) {
        tenantMasterService.deletePaymentTerms(id);
        return ResponseEntity.ok(ApiResponse.success("Payment terms deleted successfully", null));
    }

    // ===================== CLIENT TYPES =====================
    @GetMapping("/client-types")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TenantMasterResponse>>> getAllClientTypes() {
        return ResponseEntity
                .ok(ApiResponse.success("Client types fetched successfully", tenantMasterService.getAllClientTypes()));
    }

    @GetMapping("/client-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getClientTypeById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Client type fetched successfully", tenantMasterService.getClientTypeById(id)));
    }

    @PostMapping("/client-types")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createClientType(
            @Valid @RequestBody ClientTypeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Client type created successfully", tenantMasterService.createClientType(request)));
    }

    @PutMapping("/client-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> updateClientType(@PathVariable Long id,
            @Valid @RequestBody ClientTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Client type updated successfully",
                tenantMasterService.updateClientType(id, request)));
    }

    @DeleteMapping("/client-types/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClientType(@PathVariable Long id) {
        tenantMasterService.deleteClientType(id);
        return ResponseEntity.ok(ApiResponse.success("Client type deleted successfully", null));
    }

    // ===================== TENANT SETTINGS =====================
    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> getSettings() {
        return ResponseEntity
                .ok(ApiResponse.success("Settings fetched successfully", tenantMasterService.getSettings()));
    }

    @PostMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TenantMasterResponse>> createOrUpdateSettings(
            @RequestBody TenantSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Settings saved successfully",
                tenantMasterService.createOrUpdateSettings(request)));
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        return ResponseEntity.ok(Map.of(
                "tenantId", SecurityUtil.getCurrentTenantId(),
                "userId", SecurityUtil.getCurrentUserId(),
                "role", SecurityUtil.getCurrentRole()));
    }

    // ===================== RBAC — LOGIN ACCESS =====================

    @GetMapping("/rbac/login-access")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RbacLoginAccessResponse>> getLoginAccess() {
        return ResponseEntity.ok(ApiResponse.success("Login access config fetched",
                tenantMasterService.getLoginAccess()));
    }

    @PutMapping("/rbac/login-access")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RbacLoginAccessResponse>> saveLoginAccess(
            @RequestBody RbacLoginAccessRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login access config saved",
                tenantMasterService.saveLoginAccess(request)));
    }

    // ===================== HOLIDAYS =====================

    @PostMapping("/holidays")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Holiday added", tenantMasterService.createHoliday(request)));
    }

    @GetMapping("/holidays")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidays(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Holidays fetched", tenantMasterService.getHolidays(year)));
    }

    @PutMapping("/holidays/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> updateHoliday(
            @PathVariable Long id, @Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Holiday updated", tenantMasterService.updateHoliday(id, request)));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable Long id) {
        tenantMasterService.deleteHoliday(id);
        return ResponseEntity.ok(ApiResponse.success("Holiday deleted", null));
    }

}