package com.feros.api.controller;

import com.feros.api.dto.request.ConsumableRequestCreateRequest;
import com.feros.api.dto.request.ConsumableRequestIssueRequest;
import com.feros.api.dto.request.ConsumableRequestRejectRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.ConsumableRequestResponse;
import com.feros.api.service.ConsumableRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumable-requests")
@RequiredArgsConstructor
public class ConsumableRequestController {

    private final ConsumableRequestService consumableRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<ConsumableRequestResponse>> createRequest(
            @RequestBody ConsumableRequestCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("DEF request submitted", consumableRequestService.createRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<ConsumableRequestResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("DEF requests fetched", consumableRequestService.getAll()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<ConsumableRequestResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending DEF requests fetched", consumableRequestService.getPending()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ConsumableRequestResponse>>> getMyRequests() {
        return ResponseEntity.ok(ApiResponse.success("My DEF requests fetched", consumableRequestService.getMyRequests()));
    }

    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<ConsumableRequestResponse>> issueRequest(
            @PathVariable Long id, @RequestBody ConsumableRequestIssueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("DEF issued", consumableRequestService.issueRequest(id, request)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<ConsumableRequestResponse>> rejectRequest(
            @PathVariable Long id, @RequestBody ConsumableRequestRejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("DEF request rejected", consumableRequestService.rejectRequest(id, request)));
    }
}
