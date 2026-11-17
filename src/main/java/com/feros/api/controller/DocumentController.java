package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.DocumentListRow;
import com.feros.api.service.StaffProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tenant-wide document management — one place to view and renew every vehicle
 * and staff document without opening each entity's detail page.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final StaffProfileService staffProfileService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DocumentListRow>>> list(
            @RequestParam(required = false) String entity,     // VEHICLE | STAFF | ALL
            @RequestParam(required = false) String status,     // EXPIRED | EXPIRING | ACTIVE | ALL
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "30") int withinDays) {
        return ResponseEntity.ok(ApiResponse.success(
                "Documents fetched successfully",
                staffProfileService.getDocumentList(entity, status, q, withinDays)));
    }
}
