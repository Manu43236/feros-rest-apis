package com.feros.api.controller;

import com.feros.api.dto.request.TyreRequestApproveRequest;
import com.feros.api.dto.request.TyreRequestCreateRequest;
import com.feros.api.dto.request.TyreRequestRejectRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TyreRequestResponse;
import com.feros.api.service.TyreRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tyre-requests")
@RequiredArgsConstructor
public class TyreRequestController {

    private final TyreRequestService tyreRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TyreRequestResponse>> createRequest(
            @RequestBody TyreRequestCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre request submitted", tyreRequestService.createRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<TyreRequestResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Tyre requests fetched", tyreRequestService.getAll()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<TyreRequestResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending tyre requests fetched", tyreRequestService.getPending()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TyreRequestResponse>>> getMyRequests() {
        return ResponseEntity.ok(ApiResponse.success("My tyre requests fetched", tyreRequestService.getMyRequests()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<TyreRequestResponse>> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) TyreRequestApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre request approved, tyre fitted", tyreRequestService.approveRequest(id, request)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<TyreRequestResponse>> rejectRequest(
            @PathVariable Long id, @RequestBody TyreRequestRejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre request rejected", tyreRequestService.rejectRequest(id, request)));
    }
}
