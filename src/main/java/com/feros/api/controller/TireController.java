package com.feros.api.controller;

import com.feros.api.dto.request.TireRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TireFittingResponse;
import com.feros.api.dto.response.TireResponse;
import com.feros.api.service.TireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tires")
@RequiredArgsConstructor
public class TireController {

    private final TireService tireService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TireResponse>> create(@RequestBody TireRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire created", tireService.createTire(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Tires fetched", tireService.getAllTires()));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireResponse>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.success("Available tires fetched", tireService.getAvailableTires()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TireResponse>> update(@PathVariable Long id, @RequestBody TireRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tire updated", tireService.updateTire(id, request)));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TireFittingResponse>>> getTireHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tire history fetched", tireService.getTireHistory(id)));
    }

    @PatchMapping("/{id}/back-to-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TireResponse>> markBackToStock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tire marked back to stock", tireService.markBackToStock(id)));
    }
}
