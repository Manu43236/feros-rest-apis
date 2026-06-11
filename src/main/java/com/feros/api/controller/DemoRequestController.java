package com.feros.api.controller;

import com.feros.api.dto.request.DemoRequestCreateDto;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DemoRequestResponse;
import com.feros.api.enums.DemoRequestStatus;
import com.feros.api.service.DemoRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo-requests")
@RequiredArgsConstructor
public class DemoRequestController {

    private final DemoRequestService demoRequestService;

    /** Public — called from the business website contact form */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submit(@Valid @RequestBody DemoRequestCreateDto dto) {
        demoRequestService.create(dto);
        return ResponseEntity.ok(ApiResponse.success("Demo request submitted", null));
    }

    /** SUPER_ADMIN — paginated list, optionally filtered by status */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<DemoRequestResponse>>> getAll(
            @RequestParam(required = false) DemoRequestStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DemoRequestResponse> result = demoRequestService.getAll(
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success("Demo requests fetched", result));
    }

    /** SUPER_ADMIN — count of NEW requests (for badge) */
    @GetMapping("/count/new")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countNew() {
        return ResponseEntity.ok(ApiResponse.success("Count fetched",
                Map.of("count", demoRequestService.countNew())));
    }

    /** SUPER_ADMIN — update status + optional notes */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DemoRequestResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam DemoRequestStatus status,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                demoRequestService.updateStatus(id, status, notes)));
    }
}
