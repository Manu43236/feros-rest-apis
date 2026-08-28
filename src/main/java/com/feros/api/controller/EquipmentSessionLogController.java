package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentSessionLogCloseRequest;
import com.feros.api.dto.request.EquipmentSessionLogStartRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.service.EquipmentSessionLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/equipment-session-logs")
@RequiredArgsConstructor
public class EquipmentSessionLogController {

    private final EquipmentSessionLogService service;

    @GetMapping("/my/today")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getToday() {
        return ResponseEntity.ok(ApiResponse.success("Today's data fetched", service.getToday()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getMy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched", service.getMy(date)));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> start(@Valid @RequestBody EquipmentSessionLogStartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session started", service.start(request)));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> close(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentSessionLogCloseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session closed", service.close(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Session deleted", null));
    }
}
