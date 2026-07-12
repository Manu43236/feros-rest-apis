package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentInvoiceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.EquipmentInvoiceCalcResult;
import com.feros.api.dto.response.EquipmentInvoicePrefillResponse;
import com.feros.api.dto.response.EquipmentInvoiceResponse;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.service.EquipmentInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EquipmentInvoiceController {

    private final EquipmentInvoiceService invoiceService;

    // ── Calculate (E7 billing engine) ──────────────────────────────────────────

    @GetMapping("/api/v1/work-orders/{woId}/equipment-invoices/calculate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentInvoiceCalcResult>>> calculate(
            @PathVariable Long woId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Billing calculated",
                invoiceService.calculate(woId, from, to)));
    }

    // ── Prefill (must be before /{woId}/equipment-invoices to avoid path clash) ─

    @GetMapping("/api/v1/work-orders/{woId}/equipment-invoices/prefill")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentInvoicePrefillResponse>>> prefill(
            @PathVariable Long woId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Prefill data fetched",
                invoiceService.prefill(woId, from, to)));
    }

    // ── Per-work-order invoice endpoints ───────────────────────────────────────

    @PostMapping("/api/v1/work-orders/{woId}/equipment-invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentInvoiceResponse>> create(
            @PathVariable Long woId,
            @Valid @RequestBody EquipmentInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice created",
                invoiceService.create(woId, request)));
    }

    @GetMapping("/api/v1/work-orders/{woId}/equipment-invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentInvoiceResponse>>> getByWorkOrder(
            @PathVariable Long woId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched",
                invoiceService.getByWorkOrder(woId)));
    }

    // ── Client-level invoice endpoints ─────────────────────────────────────────

    @PostMapping("/api/v1/equipment-invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentInvoiceResponse>> createForClient(
            @Valid @RequestBody EquipmentInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice created",
                invoiceService.createForClient(request)));
    }

    @GetMapping("/api/v1/equipment-invoices/prefill")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentInvoicePrefillResponse>>> prefillByClient(
            @RequestParam Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Prefill data fetched",
                invoiceService.prefillByClient(clientId, from, to)));
    }

    @GetMapping("/api/v1/equipment-invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Page<EquipmentInvoiceResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EquipmentInvoiceStatus status,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched",
                invoiceService.getAll(page, size, status, clientId)));
    }

    @GetMapping("/api/v1/equipment-invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentInvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched", invoiceService.getById(id)));
    }

    @PutMapping("/api/v1/equipment-invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentInvoiceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice updated", invoiceService.update(id, request)));
    }

    @PatchMapping("/api/v1/equipment-invoices/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentInvoiceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        EquipmentInvoiceStatus newStatus = EquipmentInvoiceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                invoiceService.updateStatus(id, newStatus)));
    }

    @DeleteMapping("/api/v1/equipment-invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted", null));
    }
}
