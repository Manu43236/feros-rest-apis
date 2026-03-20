package com.feros.api.controller;

import com.feros.api.dto.request.SubscriptionPlanRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.SubscriptionPlanResponse;
import com.feros.api.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> create(
            @Valid @RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan created", planService.createPlan(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan updated", planService.updatePlan(id, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Plans fetched", planService.getActivePlans()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllIncludingInactive() {
        return ResponseEntity.ok(ApiResponse.success("Plans fetched", planService.getAllPlans()));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        planService.togglePlanStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Plan status toggled", null));
    }
}
