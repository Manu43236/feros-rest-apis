package com.feros.api.controller;

import com.feros.api.dto.request.ClientRequest;
import com.feros.api.dto.request.UserStatusRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.ClientResponse;
import com.feros.api.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAllClients() {
        return ResponseEntity.ok(ApiResponse.success(
                "Clients fetched successfully", clientService.getAllClients()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client fetched successfully", clientService.getClientById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client created successfully", clientService.createClient(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client updated successfully", clientService.updateClient(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> toggleStatus(
            @PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client status updated successfully", clientService.toggleStatus(id, request.getIsActive())));
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUpload(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bulk upload completed", clientService.bulkUpload(file)));
    }
}