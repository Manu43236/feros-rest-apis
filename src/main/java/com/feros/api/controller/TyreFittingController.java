package com.feros.api.controller;

import com.feros.api.dto.request.TyreFitRequest;
import com.feros.api.dto.request.TyreRemoveRequest;
import com.feros.api.dto.request.TyreRotationRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TyreFittingResponse;
import com.feros.api.dto.response.TyreRotationLogResponse;
import com.feros.api.service.TyreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TyreFittingController {

    private final TyreService tyreService;

    @PostMapping("/api/v1/tyre-fittings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<TyreFittingResponse>> fitTyre(@RequestBody TyreFitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre fitted", tyreService.fitTyre(request)));
    }

    @PutMapping("/api/v1/tyre-fittings/{id}/remove")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<TyreFittingResponse>> removeTyre(
            @PathVariable Long id, @RequestBody TyreRemoveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre removed", tyreService.removeTyre(id, request)));
    }

    @GetMapping("/api/v1/tyre-fittings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<TyreFittingResponse>>> getFittingHistory(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Fitting history fetched", tyreService.getFittingHistory(vehicleId)));
    }

    @PostMapping("/api/v1/tyre-rotations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<TyreRotationLogResponse>> performRotation(@RequestBody TyreRotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rotation performed", tyreService.performRotation(request)));
    }

    @GetMapping("/api/v1/tyre-rotations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<TyreRotationLogResponse>>> getRotationHistory(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Rotation history fetched", tyreService.getRotationHistory(vehicleId)));
    }
}
