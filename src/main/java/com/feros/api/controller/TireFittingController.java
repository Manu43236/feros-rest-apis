package com.feros.api.controller;

import com.feros.api.dto.request.TireFitRequest;
import com.feros.api.dto.request.TireRemoveRequest;
import com.feros.api.dto.request.TireRotationRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TireFittingResponse;
import com.feros.api.dto.response.TireRotationLogResponse;
import com.feros.api.service.TireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TireFittingController {

    private final TireService tireService;

    @PostMapping("/api/v1/tire-fittings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TireFittingResponse>> fitTire(@RequestBody TireFitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire fitted", tireService.fitTire(request)));
    }

    @PutMapping("/api/v1/tire-fittings/{id}/remove")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TireFittingResponse>> removeTire(
            @PathVariable Long id, @RequestBody TireRemoveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire removed", tireService.removeTire(id, request)));
    }

    @GetMapping("/api/v1/tire-fittings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireFittingResponse>>> getFittingHistory(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Fitting history fetched", tireService.getFittingHistory(vehicleId)));
    }

    @PostMapping("/api/v1/tire-rotations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TireRotationLogResponse>> performRotation(@RequestBody TireRotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rotation performed", tireService.performRotation(request)));
    }

    @GetMapping("/api/v1/tire-rotations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireRotationLogResponse>>> getRotationHistory(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Rotation history fetched", tireService.getRotationHistory(vehicleId)));
    }
}
