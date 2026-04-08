package com.feros.api.controller;

import com.feros.api.dto.request.MeterReadingRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.MeterReadingResponse;
import com.feros.api.service.MeterReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> create(
            @Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meter reading recorded", meterReadingService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> getAll(
            @RequestParam(required = false) Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meter readings fetched", meterReadingService.getAll(vehicleId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meter reading updated", meterReadingService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        meterReadingService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Meter reading deleted", null));
    }
}
