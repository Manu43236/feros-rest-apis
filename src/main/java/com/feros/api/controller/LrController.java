package com.feros.api.controller;

import com.feros.api.dto.request.CreateLrRequest;
import com.feros.api.dto.request.LrChargeRequest;
import com.feros.api.dto.request.LrCheckpostRequest;
import com.feros.api.dto.request.UpdateLrRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.LrChargeResponse;
import com.feros.api.dto.response.LrCheckpostResponse;
import com.feros.api.dto.response.LrResponse;
import com.feros.api.enums.LrStatus;
import com.feros.api.service.LrService;
import com.feros.api.service.impl.LrPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lrs")
@RequiredArgsConstructor
public class LrController {

    private final LrService    lrService;
    private final LrPdfService lrPdfService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('DRIVER', 'CLEANER')")
    public ResponseEntity<ApiResponse<List<LrResponse>>> getMyLrs() {
        return ResponseEntity.ok(ApiResponse.success(
                "My LRs fetched successfully", lrService.getMyLrs()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
    public ResponseEntity<ApiResponse<Page<LrResponse>>> getAllLrs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    LrStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "LRs fetched successfully", lrService.getAllLrs(page, size, search, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
    public ResponseEntity<ApiResponse<LrResponse>> getLrById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "LR fetched successfully", lrService.getLrById(id)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<LrResponse>>> getLrsByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "LRs fetched successfully", lrService.getLrsByOrder(orderId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<LrResponse>> createLr(
            @Valid @RequestBody CreateLrRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "LR created successfully", lrService.createLr(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<LrResponse>> updateLr(
            @PathVariable Long id, @RequestBody UpdateLrRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "LR updated successfully", lrService.updateLr(id, request)));
    }

    @PostMapping("/{id}/checkposts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<LrCheckpostResponse>> addCheckpost(
            @PathVariable Long id, @Valid @RequestBody LrCheckpostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Checkpost added successfully", lrService.addCheckpost(id, request)));
    }

    @GetMapping("/{id}/checkposts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<LrCheckpostResponse>>> getCheckposts(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Checkposts fetched successfully", lrService.getCheckposts(id)));
    }

    @PostMapping("/{id}/charges")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<LrChargeResponse>> addCharge(
            @PathVariable Long id, @Valid @RequestBody LrChargeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Charge added successfully", lrService.addCharge(id, request)));
    }

    @GetMapping("/{id}/charges")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<LrChargeResponse>>> getCharges(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Charges fetched successfully", lrService.getCharges(id)));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
    public ResponseEntity<byte[]> downloadLrPdf(@PathVariable Long id) {
        byte[] pdf = lrPdfService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"LR-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}/checkposts/{checkpostId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteCheckpost(
            @PathVariable Long id, @PathVariable Long checkpostId) {
        lrService.deleteCheckpost(checkpostId);
        return ResponseEntity.ok(ApiResponse.success("Checkpost deleted", null));
    }

    @DeleteMapping("/{id}/charges/{chargeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteCharge(
            @PathVariable Long id, @PathVariable Long chargeId) {
        lrService.deleteCharge(chargeId);
        return ResponseEntity.ok(ApiResponse.success("Charge deleted", null));
    }
}