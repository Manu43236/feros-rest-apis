package com.feros.api.controller;

import com.feros.api.dto.request.TireRequestApproveRequest;
import com.feros.api.dto.request.TireRequestCreateRequest;
import com.feros.api.dto.request.TireRequestRejectRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TireRequestResponse;
import com.feros.api.service.TireRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tire-requests")
@RequiredArgsConstructor
public class TireRequestController {

    private final TireRequestService tireRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TireRequestResponse>> createRequest(
            @RequestBody TireRequestCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire request submitted", tireRequestService.createRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<TireRequestResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Tire requests fetched", tireRequestService.getAll()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<TireRequestResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending tire requests fetched", tireRequestService.getPending()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireRequestResponse>>> getMyRequests() {
        return ResponseEntity.ok(ApiResponse.success("My tire requests fetched", tireRequestService.getMyRequests()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<TireRequestResponse>> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) TireRequestApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire request approved, tire fitted", tireRequestService.approveRequest(id, request)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<TireRequestResponse>> rejectRequest(
            @PathVariable Long id, @RequestBody TireRequestRejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire request rejected", tireRequestService.rejectRequest(id, request)));
    }
}
