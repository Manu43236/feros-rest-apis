package com.feros.api.controller;

import com.feros.api.dto.request.CreateLrRequest;
import com.feros.api.dto.request.LrChargeRequest;
import com.feros.api.dto.request.LrCheckpostRequest;
import com.feros.api.dto.request.UpdateLrRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.LrChargeResponse;
import com.feros.api.dto.response.LrCheckpostResponse;
import com.feros.api.dto.response.LrResponse;
import com.feros.api.service.LrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lrs")
@RequiredArgsConstructor
public class LrController {

    private final LrService lrService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<LrResponse>>> getAllLrs() {
        return ResponseEntity.ok(ApiResponse.success(
                "LRs fetched successfully", lrService.getAllLrs()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
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
}