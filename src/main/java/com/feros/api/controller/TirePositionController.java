package com.feros.api.controller;

import com.feros.api.dto.request.TirePositionRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TirePositionResponse;
import com.feros.api.service.TireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tire-positions")
@RequiredArgsConstructor
public class TirePositionController {

    private final TireService tireService;

    @PostMapping("/auto-setup")
    public ResponseEntity<ApiResponse<List<TirePositionResponse>>> autoSetup(@RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Positions auto-configured", tireService.autoSetupPositions(vehicleId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TirePositionResponse>> create(@RequestBody TirePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position created", tireService.addPosition(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TirePositionResponse>>> getAll(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Positions fetched", tireService.getPositionsForVehicle(vehicleId)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<TirePositionResponse>>> getCurrent(
            @RequestParam Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success("Current positions fetched", tireService.getCurrentPositionsForVehicle(vehicleId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TirePositionResponse>> update(
            @PathVariable Long id, @RequestBody TirePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position updated", tireService.updatePosition(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tireService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Position removed", null));
    }
}
