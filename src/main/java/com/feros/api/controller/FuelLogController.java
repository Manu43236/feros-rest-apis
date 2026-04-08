package com.feros.api.controller;

import com.feros.api.dto.request.FuelLogRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.FuelLogResponse;
import com.feros.api.service.FuelLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fuel-logs")
@RequiredArgsConstructor
public class FuelLogController {

    private final FuelLogService fuelLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<FuelLogResponse>> create(@RequestBody FuelLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel log added", fuelLogService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FuelLogResponse>>> getAll(
            @RequestParam(required = false) Long vehicleId) {
        List<FuelLogResponse> result = vehicleId != null
                ? fuelLogService.getByVehicle(vehicleId)
                : fuelLogService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Fuel logs fetched", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FuelLogResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fuel log fetched", fuelLogService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FuelLogResponse>> update(
            @PathVariable Long id, @RequestBody FuelLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel log updated", fuelLogService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fuelLogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fuel log deleted", null));
    }
}
