package com.feros.api.controller;

import com.feros.api.dto.request.TripExpenseApproveRequest;
import com.feros.api.dto.request.TripExpenseRequest;
import com.feros.api.dto.request.TripExpenseSettleRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TripExpenseResponse;
import com.feros.api.enums.TripExpenseStatus;
import com.feros.api.service.TripExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TripExpenseController {

    private final TripExpenseService tripExpenseService;

    // Supervisor: Create draft expense sheet for a delivered LR
    @PostMapping("/lr/{lrId}/trip-expense")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> createDraft(
            @PathVariable Long lrId,
            @RequestBody TripExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense sheet created", tripExpenseService.createDraft(lrId, request)));
    }

    // Supervisor: Update draft (items, advance, trip days)
    @PutMapping("/lr/{lrId}/trip-expense")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> updateDraft(
            @PathVariable Long lrId,
            @RequestBody TripExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense sheet updated", tripExpenseService.updateDraft(lrId, request)));
    }

    // Supervisor: Submit draft to admin for approval
    @PostMapping("/lr/{lrId}/trip-expense/submit")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> submit(@PathVariable Long lrId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense sheet submitted", tripExpenseService.submit(lrId)));
    }

    // Both: View expense sheet for an LR
    @GetMapping("/lr/{lrId}/trip-expense")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> getByLrId(@PathVariable Long lrId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense sheet fetched", tripExpenseService.getByLrId(lrId)));
    }

    // Both: List all expense sheets (optional filter by status)
    @GetMapping("/trip-expenses")
    public ResponseEntity<ApiResponse<List<TripExpenseResponse>>> getAll(
            @RequestParam(required = false) TripExpenseStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expenses fetched", tripExpenseService.getAll(status)));
    }

    // Admin: Edit item amounts and approve
    @PutMapping("/trip-expenses/{id}/approve")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> approve(
            @PathVariable Long id,
            @RequestBody TripExpenseApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense sheet approved", tripExpenseService.approve(id, request)));
    }

    // Both (Admin + Supervisor): Record settlement
    @PostMapping("/trip-expenses/{id}/settle")
    public ResponseEntity<ApiResponse<TripExpenseResponse>> settle(
            @PathVariable Long id,
            @RequestBody TripExpenseSettleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip expense settled", tripExpenseService.settle(id, request)));
    }
}
