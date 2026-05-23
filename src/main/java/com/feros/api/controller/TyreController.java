package com.feros.api.controller;

import com.feros.api.dto.request.TyreRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TyreFittingResponse;
import com.feros.api.dto.response.TyreResponse;
import com.feros.api.service.TyreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tyres")
@RequiredArgsConstructor
public class TyreController {

    private final TyreService tyreService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TyreResponse>> create(@RequestBody TyreRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre created", tyreService.createTyre(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TyreResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Tyres fetched", tyreService.getAllTyres()));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TyreResponse>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.success("Available tyres fetched", tyreService.getAvailableTyres()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TyreResponse>> update(@PathVariable Long id, @RequestBody TyreRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tyre updated", tyreService.updateTyre(id, request)));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<TyreFittingResponse>>> getTyreHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tyre history fetched", tyreService.getTyreHistory(id)));
    }

    @PatchMapping("/{id}/back-to-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<TyreResponse>> markBackToStock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tyre marked back to stock", tyreService.markBackToStock(id)));
    }
}
