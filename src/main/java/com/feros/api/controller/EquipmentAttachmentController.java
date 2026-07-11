package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentAttachmentRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.EquipmentAttachmentResponse;
import com.feros.api.service.EquipmentAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment/attachments")
@RequiredArgsConstructor
public class EquipmentAttachmentController {

    private final EquipmentAttachmentService attachmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<EquipmentAttachmentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Attachments fetched", attachmentService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF','SUPERVISOR')")
    public ResponseEntity<ApiResponse<EquipmentAttachmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Attachment fetched", attachmentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentAttachmentResponse>> create(
            @RequestBody EquipmentAttachmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attachment created", attachmentService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentAttachmentResponse>> update(
            @PathVariable Long id, @RequestBody EquipmentAttachmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attachment updated", attachmentService.update(id, request)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<EquipmentAttachmentResponse>> setActive(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("isActive"));
        return ResponseEntity.ok(ApiResponse.success("Attachment updated", attachmentService.setActive(id, active)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted", null));
    }
}
