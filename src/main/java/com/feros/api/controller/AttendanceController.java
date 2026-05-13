package com.feros.api.controller;

import com.feros.api.dto.request.AttendanceRequest;
import com.feros.api.dto.request.BulkAttendanceRequest;
import com.feros.api.dto.request.MarkMobilePresentRequest;
import com.feros.api.dto.request.MarkOwnAttendanceRequest;
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

    @PostMapping("/attendance/my")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'DRIVER', 'CLEANER', 'SERVICE_MEN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markOwnAttendance(
            @Valid @RequestBody MarkOwnAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance marked successfully", attendanceService.markOwnAttendance(request)));
    }

    @PostMapping("/attendance/my/mark-present")
    @PreAuthorize("hasAnyRole('DRIVER', 'CLEANER', 'SUPERVISOR', 'SERVICE_MEN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markMobilePresent(
            @RequestBody MarkMobilePresentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance marked successfully", attendanceService.markMobilePresent(request)));
    }

    @GetMapping("/attendance/my/today-status")
    @PreAuthorize("hasAnyRole('DRIVER', 'CLEANER', 'SUPERVISOR', 'SERVICE_MEN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendanceStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                "Today attendance status fetched", attendanceService.getTodayAttendanceStatus()));
    }

    @GetMapping("/attendance/my")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'DRIVER', 'CLEANER', 'SERVICE_MEN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMyAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance fetched successfully", attendanceService.getMyAttendance(from, to)));
    }

    @GetMapping("/attendance/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getPendingAttendance() {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending attendance fetched successfully", attendanceService.getPendingAttendance()));
    }

    @PutMapping("/attendance/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> approveAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance approved successfully", attendanceService.approveAttendance(id)));
    }

    @PutMapping("/attendance/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> rejectAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance rejected successfully", attendanceService.rejectAttendance(id)));
    }

    @GetMapping("/attendance/rejected")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getRejectedAttendance() {
        return ResponseEntity.ok(ApiResponse.success(
                "Rejected attendance fetched", attendanceService.getRejectedAttendance()));
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