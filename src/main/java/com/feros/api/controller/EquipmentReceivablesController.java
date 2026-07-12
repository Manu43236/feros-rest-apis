package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentAdvanceRequest;
import com.feros.api.dto.request.EquipmentPaymentRequest;
import com.feros.api.dto.request.EquipmentRetentionReleaseRequest;
import com.feros.api.dto.response.EquipmentAdvanceResponse;
import com.feros.api.dto.response.EquipmentPaymentResponse;
import com.feros.api.dto.response.EquipmentRetentionReleaseResponse;
import com.feros.api.dto.response.WoReceivablesSummaryResponse;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.service.EquipmentReceivablesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EquipmentReceivablesController {

    private final EquipmentReceivablesService receivablesService;

    // ── Payments ──────────────────────────────────────────────────────────────

    @PostMapping("/api/v1/work-orders/{woId}/equipment-invoices/{invId}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentPaymentResponse>> recordPayment(
            @PathVariable Long woId, @PathVariable Long invId,
            @RequestBody EquipmentPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", receivablesService.recordPayment(woId, invId, req)));
    }

    @GetMapping("/api/v1/work-orders/{woId}/equipment-invoices/{invId}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentPaymentResponse>>> listPayments(
            @PathVariable Long woId, @PathVariable Long invId) {
        return ResponseEntity.ok(ApiResponse.success("Payments fetched", receivablesService.listPayments(woId, invId)));
    }

    @DeleteMapping("/api/v1/work-orders/{woId}/equipment-invoices/{invId}/payments/{payId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Long woId, @PathVariable Long invId, @PathVariable Long payId) {
        receivablesService.deletePayment(woId, invId, payId);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted", null));
    }

    // ── Advances ──────────────────────────────────────────────────────────────

    @PostMapping("/api/v1/work-orders/{woId}/advances")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentAdvanceResponse>> recordAdvance(
            @PathVariable Long woId, @RequestBody EquipmentAdvanceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Advance recorded", receivablesService.recordAdvance(woId, req)));
    }

    @GetMapping("/api/v1/work-orders/{woId}/advances")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentAdvanceResponse>>> listAdvances(@PathVariable Long woId) {
        return ResponseEntity.ok(ApiResponse.success("Advances fetched", receivablesService.listAdvances(woId)));
    }

    @DeleteMapping("/api/v1/work-orders/{woId}/advances/{advId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAdvance(@PathVariable Long woId, @PathVariable Long advId) {
        receivablesService.deleteAdvance(woId, advId);
        return ResponseEntity.ok(ApiResponse.success("Advance deleted", null));
    }

    // ── Retention Releases ────────────────────────────────────────────────────

    @PostMapping("/api/v1/work-orders/{woId}/retention-releases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentRetentionReleaseResponse>> recordRetentionRelease(
            @PathVariable Long woId, @RequestBody EquipmentRetentionReleaseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Retention release recorded", receivablesService.recordRetentionRelease(woId, req)));
    }

    @GetMapping("/api/v1/work-orders/{woId}/retention-releases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<EquipmentRetentionReleaseResponse>>> listRetentionReleases(@PathVariable Long woId) {
        return ResponseEntity.ok(ApiResponse.success("Retention releases fetched", receivablesService.listRetentionReleases(woId)));
    }

    @DeleteMapping("/api/v1/work-orders/{woId}/retention-releases/{relId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRetentionRelease(@PathVariable Long woId, @PathVariable Long relId) {
        receivablesService.deleteRetentionRelease(woId, relId);
        return ResponseEntity.ok(ApiResponse.success("Retention release deleted", null));
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/work-orders/{woId}/receivables-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<WoReceivablesSummaryResponse>> getSummary(@PathVariable Long woId) {
        return ResponseEntity.ok(ApiResponse.success("Receivables summary fetched", receivablesService.getSummary(woId)));
    }
}
