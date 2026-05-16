package com.feros.api.controller;

import com.feros.api.dto.request.TirePositionRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TirePositionResponse;
import com.feros.api.service.TireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tire-positions")
@RequiredArgsConstructor
public class TirePositionController {

    private final TireService tireService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TirePositionResponse>> create(@RequestBody TirePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position created", tireService.addPosition(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TirePositionResponse>>> getAll(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Positions fetched", tireService.getPositionsForVehicle(vehicleId)));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TirePositionResponse>>> getCurrent(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Current positions fetched", tireService.getCurrentPositionsForVehicle(vehicleId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TirePositionResponse>> update(
            @PathVariable Long id, @RequestBody TirePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position updated", tireService.updatePosition(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tireService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Position removed", null));
    }
}
