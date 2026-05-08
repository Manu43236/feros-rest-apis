package com.feros.api.controller;

import com.feros.api.dto.request.CreateInvoiceRequest;
import com.feros.api.dto.request.InvoicePaymentRequest;
import com.feros.api.dto.request.UpdateInvoiceRequest;
import com.feros.api.dto.request.UpdateInvoiceStatusRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.InvoicePaymentResponse;
import com.feros.api.dto.response.InvoiceResponse;
import com.feros.api.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoices fetched successfully", invoiceService.getAllInvoices()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice fetched successfully", invoiceService.getInvoiceById(id)));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoices fetched successfully", invoiceService.getInvoicesByClient(clientId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice created successfully", invoiceService.createInvoice(request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice status updated successfully",
                invoiceService.updateInvoiceStatus(id, request)));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<InvoicePaymentResponse>> recordPayment(
            @PathVariable Long id, @Valid @RequestBody InvoicePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment recorded successfully", invoiceService.recordPayment(id, request)));
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoicePaymentResponse>>> getPayments(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payments fetched successfully", invoiceService.getPayments(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id, @RequestBody UpdateInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice updated successfully", invoiceService.updateInvoice(id, request)));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Long id, @PathVariable Long paymentId) {
        invoiceService.deletePayment(id, paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted", null));
    }

    @GetMapping("/invoiced-lr-ids")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<Long>>> getInvoicedLrIds() {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoiced LR IDs fetched", invoiceService.getInvoicedLrIds()));
    }
}