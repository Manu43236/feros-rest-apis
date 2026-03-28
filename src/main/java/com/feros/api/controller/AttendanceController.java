package com.feros.api.controller;

import com.feros.api.dto.request.AttendanceRequest;
import com.feros.api.dto.request.BulkAttendanceRequest;
import com.feros.api.dto.request.ReviewTripProofRequest;
import com.feros.api.dto.request.TripProofRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.AttendanceResponse;
import com.feros.api.dto.response.TripProofResponse;
import com.feros.api.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ===================== ATTENDANCE =====================
    @PostMapping("/attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance marked successfully", attendanceService.markAttendance(request)));
    }

    @PostMapping("/attendance/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markBulkAttendance(
            @Valid @RequestBody BulkAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bulk attendance marked successfully",
                attendanceService.markBulkAttendance(request)));
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance fetched successfully",
                attendanceService.getAttendanceByDate(date)));
    }

    @GetMapping("/attendance/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance fetched successfully",
                attendanceService.getAttendanceByUser(userId, from, to)));
    }

    @PutMapping("/attendance/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance updated successfully",
                attendanceService.updateAttendance(id, request)));
    }

    // ===================== TRIP PROOFS =====================
    @PostMapping("/trip-proofs/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'DRIVER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<TripProofResponse>> addTripProof(
            @PathVariable Long userId, @Valid @RequestBody TripProofRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip proof added successfully",
                attendanceService.addTripProof(userId, request)));
    }

    @GetMapping("/trip-proofs/lr/{lrId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<TripProofResponse>>> getTripProofsByLr(
            @PathVariable Long lrId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip proofs fetched successfully",
                attendanceService.getTripProofsByLr(lrId)));
    }

    @GetMapping("/trip-proofs/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER')")
    public ResponseEntity<ApiResponse<List<TripProofResponse>>> getTripProofsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip proofs fetched successfully",
                attendanceService.getTripProofsByUser(userId)));
    }

    @PutMapping("/trip-proofs/{proofId}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TripProofResponse>> reviewTripProof(
            @PathVariable Long proofId, @RequestBody ReviewTripProofRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trip proof reviewed successfully",
                attendanceService.reviewTripProof(proofId, request)));
    }
}