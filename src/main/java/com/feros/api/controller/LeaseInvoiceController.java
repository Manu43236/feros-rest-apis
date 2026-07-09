package com.feros.api.controller;

import com.feros.api.dto.request.LeaseInvoiceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.LeaseInvoicePrefillResponse;
import com.feros.api.dto.response.LeaseInvoiceResponse;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.service.LeaseInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LeaseInvoiceController {

    private final LeaseInvoiceService invoiceService;

    @GetMapping("/api/v1/vehicle-leases/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<LeaseInvoiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched", invoiceService.getAll()));
    }

    @GetMapping("/api/v1/vehicle-leases/{leaseId}/invoices/prefill")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<LeaseInvoicePrefillResponse>>> prefill(
            @PathVariable Long leaseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Prefill computed",
                invoiceService.prefill(leaseId, from, to)));
    }

    @PostMapping("/api/v1/vehicle-leases/{leaseId}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<LeaseInvoiceResponse>> create(
            @PathVariable Long leaseId,
            @Valid @RequestBody LeaseInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice created",
                invoiceService.create(leaseId, request)));
    }

    @GetMapping("/api/v1/vehicle-leases/{leaseId}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<LeaseInvoiceResponse>>> getByLease(@PathVariable Long leaseId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched",
                invoiceService.getByLease(leaseId)));
    }

    @GetMapping("/api/v1/vehicle-leases/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<LeaseInvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched",
                invoiceService.getById(id)));
    }

    @PutMapping("/api/v1/vehicle-leases/invoices/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<LeaseInvoiceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        EquipmentInvoiceStatus status = EquipmentInvoiceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                invoiceService.updateStatus(id, status)));
    }

    @DeleteMapping("/api/v1/vehicle-leases/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted", null));
    }
}
