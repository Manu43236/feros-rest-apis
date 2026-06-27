package com.feros.api.controller;

import com.feros.api.dto.request.ClientRequest;
import com.feros.api.dto.request.UserStatusRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.ClientDivisionResponse;
import com.feros.api.dto.response.ClientResponse;
import com.feros.api.service.ClientService;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> getAllClients(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search) {
        return ResponseEntity.ok(ApiResponse.success(
                "Clients fetched successfully", clientService.getAllClients(page, size, search)));
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

    // ── Divisions ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/divisions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ClientDivisionResponse>>> getDivisions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Divisions fetched", clientService.getDivisions(id)));
    }

    @PostMapping("/{id}/divisions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClientDivisionResponse>> addDivision(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("Division name is required"));
        return ResponseEntity.ok(ApiResponse.success("Division added", clientService.addDivision(id, name)));
    }

    @DeleteMapping("/{id}/divisions/{divisionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDivision(
            @PathVariable Long id, @PathVariable Long divisionId) {
        clientService.deleteDivision(id, divisionId);
        return ResponseEntity.ok(ApiResponse.success("Division removed", null));
    }
}