package com.feros.api.controller;

import com.feros.api.dto.request.CreditNoteRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.CreditNoteResponse;
import com.feros.api.enums.CreditNoteStatus;
import com.feros.api.service.CreditNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<CreditNoteResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit notes fetched successfully", creditNoteService.getAll()));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<CreditNoteResponse>>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit notes fetched successfully", creditNoteService.getByClient(clientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit note fetched successfully", creditNoteService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> create(
            @Valid @RequestBody CreditNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit note created successfully", creditNoteService.create(request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> updateStatus(
            @PathVariable Long id, @RequestParam CreditNoteStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit note status updated", creditNoteService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        creditNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Credit note deleted successfully", null));
    }
}
