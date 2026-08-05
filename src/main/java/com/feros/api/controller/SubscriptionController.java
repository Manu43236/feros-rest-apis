package com.feros.api.controller;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ConfirmSubscriptionPaymentRequest;
import com.feros.api.dto.request.CorrectSubscriptionRequest;
import com.feros.api.dto.request.CreateProformaInvoiceRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionInvoiceSummaryResponse;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.service.SubscriptionService;
import com.feros.api.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ─── SUPER_ADMIN actions ──────────────────────────────────────────────────

    @PostMapping("/{tenantId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> activate(
            @PathVariable Long tenantId,
            @Valid @RequestBody ActivateSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription activated",
                subscriptionService.activate(tenantId, request)));
    }

    @PostMapping("/{tenantId}/extend-trial")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> extendTrial(
            @PathVariable Long tenantId,
            @Valid @RequestBody ExtendSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trial extended",
                subscriptionService.extendTrial(tenantId, request)));
    }

    @PostMapping("/{tenantId}/extend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> extend(
            @PathVariable Long tenantId,
            @Valid @RequestBody ExtendSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription extended",
                subscriptionService.extendSubscription(tenantId, request)));
    }

    @PostMapping("/{tenantId}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> suspend(
            @PathVariable Long tenantId,
            @Valid @RequestBody SuspendSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription suspended",
                subscriptionService.suspend(tenantId, request)));
    }

    @PostMapping("/{tenantId}/reactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> reactivate(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success("Subscription reactivated",
                subscriptionService.reactivate(tenantId, notes)));
    }

    @GetMapping("/{tenantId}/history")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionHistoryResponse>>> getHistory(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.success("History fetched",
                subscriptionService.getHistory(tenantId)));
    }

    @GetMapping("/{tenantId}/invoices")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionInvoiceResponse>>> getInvoices(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched",
                subscriptionService.getInvoices(tenantId)));
    }

    @GetMapping("/{tenantId}/current")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> getCurrent(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.success("Current subscription fetched",
                subscriptionService.getCurrentSubscription(tenantId)));
    }

    @PatchMapping("/{tenantId}/correct")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> correct(
            @PathVariable Long tenantId,
            @Valid @RequestBody CorrectSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription corrected",
                subscriptionService.correctSubscription(tenantId, request)));
    }

    @PostMapping("/{tenantId}/history/{historyId}/invoice")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionInvoiceResponse>> generateInvoice(
            @PathVariable Long tenantId, @PathVariable Long historyId) {
        return ResponseEntity.ok(ApiResponse.success("Invoice generated",
                subscriptionService.generateInvoiceForHistory(tenantId, historyId)));
    }

    @PostMapping("/{tenantId}/invoices/proforma")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionInvoiceResponse>> createProforma(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateProformaInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Proforma invoice created",
                subscriptionService.createProformaInvoice(tenantId, request)));
    }

    @PostMapping("/{tenantId}/invoices/{invoiceId}/confirm")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionInvoiceResponse>> confirmPayment(
            @PathVariable Long tenantId, @PathVariable Long invoiceId,
            @Valid @RequestBody ConfirmSubscriptionPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed, tax invoice issued",
                subscriptionService.confirmPayment(tenantId, invoiceId, request)));
    }

    @GetMapping("/{tenantId}/invoices/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionInvoiceSummaryResponse>> getInvoiceSummary(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.success("Summary fetched",
                subscriptionService.getInvoiceSummary(tenantId)));
    }

    @GetMapping("/{tenantId}/invoices/{invoiceId}/pdf")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> getInvoicePdf(
            @PathVariable Long tenantId, @PathVariable Long invoiceId) {
        byte[] pdf = subscriptionService.generateInvoicePdf(tenantId, invoiceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"invoice-" + invoiceId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ─── Tenant self-service ──────────────────────────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER', 'SERVICE_MANAGER', 'TECHNICIAN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<SubscriptionHistoryResponse>> getMyCurrent() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Subscription fetched",
                subscriptionService.getCurrentSubscription(tenantId)));
    }

    @GetMapping("/my/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<SubscriptionInvoiceResponse>>> getMyInvoices() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched",
                subscriptionService.getInvoices(tenantId)));
    }

    @GetMapping("/my/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionInvoiceResponse>> getMyInvoiceById(
            @PathVariable Long invoiceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched",
                subscriptionService.getInvoiceById(tenantId, invoiceId)));
    }

    @GetMapping("/my/invoices/{invoiceId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<byte[]> getMyInvoicePdf(@PathVariable Long invoiceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        byte[] pdf = subscriptionService.generateInvoicePdf(tenantId, invoiceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"subscription-invoice-" + invoiceId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
