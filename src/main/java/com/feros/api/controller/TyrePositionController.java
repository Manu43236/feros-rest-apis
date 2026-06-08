package com.feros.api.controller;

import com.feros.api.dto.request.TyrePositionRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TyrePositionResponse;
import com.feros.api.service.TyreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tyre-positions")
@RequiredArgsConstructor
public class TyrePositionController {

    private final TyreService tyreService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TyrePositionResponse>> create(@RequestBody TyrePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position created", tyreService.addPosition(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<List<TyrePositionResponse>>> getAll(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Positions fetched", tyreService.getPositionsForVehicle(vehicleId)));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TyrePositionResponse>>> getCurrent(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Current positions fetched", tyreService.getCurrentPositionsForVehicle(vehicleId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TyrePositionResponse>> update(
            @PathVariable Long id, @RequestBody TyrePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position updated", tyreService.updatePosition(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tyreService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Position removed", null));
    }
}
