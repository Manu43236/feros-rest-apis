package com.feros.api.controller;

import com.feros.api.dto.request.ClientAdvanceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.ClientAdvanceResponse;
import com.feros.api.service.ClientAdvanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-advances")
@RequiredArgsConstructor
public class ClientAdvanceController {

    private final ClientAdvanceService clientAdvanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ClientAdvanceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Client advances fetched successfully", clientAdvanceService.getAll()));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ClientAdvanceResponse>>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client advances fetched successfully", clientAdvanceService.getByClient(clientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ClientAdvanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client advance fetched successfully", clientAdvanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ClientAdvanceResponse>> create(
            @Valid @RequestBody ClientAdvanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client advance recorded successfully", clientAdvanceService.create(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clientAdvanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Client advance deleted successfully", null));
    }
}
