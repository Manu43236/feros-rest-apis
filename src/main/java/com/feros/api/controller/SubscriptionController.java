package com.feros.api.controller;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.request.UpgradeRequestRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.dto.response.UpgradeRequestResponse;
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

    // ─── SUPER_ADMIN actions (tenant-specific) ────────────────────────────────

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

    // ─── Tenant self-service (ADMIN can view own subscription) ───────────────

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER', 'SERVICE_MEN', 'STORE_KEEPER')")
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

    // ─── Upgrade Requests ─────────────────────────────────────────────────────

    @PostMapping("/upgrade-request")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<UpgradeRequestResponse>> submitUpgradeRequest(
            @Valid @RequestBody UpgradeRequestRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Upgrade request submitted",
                subscriptionService.submitUpgradeRequest(tenantId, request)));
    }

    @GetMapping("/upgrade-requests")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UpgradeRequestResponse>>> getUpgradeRequests() {
        return ResponseEntity.ok(ApiResponse.success("Upgrade requests fetched",
                subscriptionService.getUpgradeRequests()));
    }

    @PatchMapping("/upgrade-requests/{id}/dismiss")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> dismissUpgradeRequest(@PathVariable Long id) {
        subscriptionService.dismissUpgradeRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Request dismissed", null));
    }
}
