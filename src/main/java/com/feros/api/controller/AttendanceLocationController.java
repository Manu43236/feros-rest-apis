package com.feros.api.controller;

import com.feros.api.dto.request.AttendanceLocationRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.AttendanceLocationResponse;
import com.feros.api.service.AttendanceLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-locations")
@RequiredArgsConstructor
public class AttendanceLocationController {

    private final AttendanceLocationService attendanceLocationService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceLocationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance locations fetched", attendanceLocationService.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceLocationResponse>> create(
            @Valid @RequestBody AttendanceLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance location created", attendanceLocationService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceLocationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AttendanceLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance location updated", attendanceLocationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        attendanceLocationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance location deleted", null));
    }
}
