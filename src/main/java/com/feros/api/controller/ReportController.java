package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.report.*;
import com.feros.api.service.ReportService;
import com.feros.api.util.ReportExportUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF')")
public class ReportController {

    private final ReportService reportService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // ── Fleet Status ──────────────────────────────────────────────────────────

    @GetMapping("/vehicles/fleet-status")
    public ResponseEntity<ApiResponse<List<FleetStatusRow>>> getFleetStatus(
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date) {
        LocalDate reportDate = date != null ? date : TimeUtil.today();
        return ResponseEntity.ok(ApiResponse.success(
                "Fleet status fetched", reportService.getFleetStatus(reportDate)));
    }

    @GetMapping("/vehicles/fleet-status/export")
    public ResponseEntity<byte[]> exportFleetStatus(
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
            @RequestParam(defaultValue = "csv") String format) {
        LocalDate reportDate = date != null ? date : TimeUtil.today();
        List<FleetStatusRow> rows = reportService.getFleetStatus(reportDate);
        String[] headers = {"Vehicle No.", "Type", "Ownership", "Status", "Driver", "Cleaner", "Trip Scope"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(), r.getOwnershipType(),
                r.getCurrentStatus(), r.getCurrentDriverName(), r.getCurrentCleanerName(), r.getTripScope()
        }).toList();
        return export("fleet-status-" + reportDate, "Fleet Status Report — " + reportDate, headers, data, format);
    }

    // ── Fuel & Mileage ────────────────────────────────────────────────────────

    @GetMapping("/vehicles/fuel-mileage")
    public ResponseEntity<ApiResponse<List<FuelMileageRow>>> getFuelMileage(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Fuel & mileage fetched", reportService.getFuelMileage(startDate, endDate)));
    }

    @GetMapping("/vehicles/fuel-mileage/export")
    public ResponseEntity<byte[]> exportFuelMileage(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<FuelMileageRow> rows = reportService.getFuelMileage(startDate, endDate);
        String[] headers = {"Vehicle No.", "Type", "Fill Ups", "Total Litres", "Total Cost (₹)",
                "Opening KM", "Closing KM", "Total KM", "Mileage (km/L)"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(),
                String.valueOf(r.getFillCount()),
                safe(r.getTotalLitresFilled()), safe(r.getTotalFuelCost()),
                safe(r.getOpeningOdometer()), safe(r.getClosingOdometer()),
                safe(r.getTotalKm()), safe(r.getMileageKmPerLitre())
        }).toList();
        return export("fuel-mileage-" + startDate + "-" + endDate,
                "Fuel & Mileage — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Breakdowns ────────────────────────────────────────────────────────────

    @GetMapping("/vehicles/breakdowns")
    public ResponseEntity<ApiResponse<List<BreakdownReportRow>>> getBreakdowns(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Breakdowns fetched", reportService.getBreakdowns(startDate, endDate)));
    }

    @GetMapping("/vehicles/breakdowns/export")
    public ResponseEntity<byte[]> exportBreakdowns(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<BreakdownReportRow> rows = reportService.getBreakdowns(startDate, endDate);
        String[] headers = {"Vehicle No.", "Type", "Breakdown Date", "Location",
                "Type", "Reason", "Status", "Days Lost", "Reported By"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(),
                r.getBreakdownDate() != null ? r.getBreakdownDate().toLocalDate().toString() : "—",
                safe(r.getLocation()), r.getBreakdownType(), safe(r.getReason()), r.getStatus(),
                r.getDaysLost() != null ? r.getDaysLost().toString() : "Ongoing",
                r.getReportedBy()
        }).toList();
        return export("breakdowns-" + startDate + "-" + endDate,
                "Breakdown Report — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Document Expiry ───────────────────────────────────────────────────────

    @GetMapping("/vehicles/document-expiry")
    public ResponseEntity<ApiResponse<List<DocumentExpiryRow>>> getDocumentExpiry(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document expiry fetched", reportService.getDocumentExpiry(days)));
    }

    @GetMapping("/vehicles/document-expiry/export")
    public ResponseEntity<byte[]> exportDocumentExpiry(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "csv") String format) {
        List<DocumentExpiryRow> rows = reportService.getDocumentExpiry(days);
        String[] headers = {"Vehicle No.", "Type", "Document", "Document No.",
                "Expiry Date", "Days Left", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(),
                r.getDocumentType(), safe(r.getDocumentNumber()),
                r.getExpiryDate().toString(), String.valueOf(r.getDaysLeft()), r.getExpiryStatus()
        }).toList();
        return export("document-expiry", "Vehicle Document Expiry Report", headers, data, format);
    }

    // ── Maintenance & Service ─────────────────────────────────────────────────

    @GetMapping("/vehicles/maintenance-service")
    public ResponseEntity<ApiResponse<List<MaintenanceServiceRow>>> getMaintenanceService(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Maintenance & service fetched", reportService.getMaintenanceService(startDate, endDate)));
    }

    @GetMapping("/vehicles/maintenance-service/export")
    public ResponseEntity<byte[]> exportMaintenanceService(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<MaintenanceServiceRow> rows = reportService.getMaintenanceService(startDate, endDate);
        String[] headers = {"Vehicle No.", "Type", "Service No.", "Service Date", "Completed Date",
                "Service Type", "Triggered By", "Tasks", "Total Cost (₹)", "Status", "Vendor", "Next Due KM"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(),
                safe(r.getServiceNumber()),
                r.getServiceDate() != null ? r.getServiceDate().toString() : "—",
                r.getCompletedDate() != null ? r.getCompletedDate().toString() : "—",
                r.getServiceType(), r.getTriggeredBy(), String.valueOf(r.getTaskCount()),
                safe(r.getTotalCost()), r.getStatus(), safe(r.getVendorName()),
                r.getNextServiceDueOdometer() != null ? r.getNextServiceDueOdometer().toString() : "—"
        }).toList();
        return export("maintenance-service-" + startDate + "-" + endDate,
                "Maintenance & Service — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Attendance Daily Register ─────────────────────────────────────────────────

    @GetMapping("/attendance/daily")
    public ResponseEntity<ApiResponse<List<AttendanceDailyRow>>> getAttendanceDaily(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance daily fetched", reportService.getAttendanceDaily(startDate, endDate)));
    }

    @GetMapping("/attendance/daily/export")
    public ResponseEntity<byte[]> exportAttendanceDaily(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<AttendanceDailyRow> rows = reportService.getAttendanceDaily(startDate, endDate);
        String[] headers = {"Date", "Employee", "Role", "Vehicle", "Type", "Mark In", "Mark Out", "Hours", "Approval", "Leave Type", "Remarks"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getAttendanceDate().toString(), r.getEmployeeName(), r.getRole(),
                safe(r.getVehicleRegistrationNumber()), r.getAttendanceType(),
                r.getMarkedAt() != null ? r.getMarkedAt().toLocalTime().toString() : "—",
                r.getMarkedOutAt() != null ? r.getMarkedOutAt().toLocalTime().toString() : "—",
                r.getHoursWorked() != null ? r.getHoursWorked() + " h" : "—",
                r.getApprovalStatus(), safe(r.getLeaveType()), safe(r.getRemarks())
        }).toList();
        return export("attendance-daily-" + startDate + "-" + endDate,
                "Attendance Daily Register — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Attendance Summary ────────────────────────────────────────────────────────

    @GetMapping("/attendance/summary")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryRow>>> getAttendanceSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance summary fetched", reportService.getAttendanceSummary(startDate, endDate)));
    }

    @GetMapping("/attendance/summary/export")
    public ResponseEntity<byte[]> exportAttendanceSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<AttendanceSummaryRow> rows = reportService.getAttendanceSummary(startDate, endDate);
        String[] headers = {"Employee", "Role", "Vehicle", "Present", "Absent", "Leave", "Half Day", "Other", "Total", "Present %"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getEmployeeName(), r.getRole(), safe(r.getVehicleRegistrationNumber()),
                String.valueOf(r.getPresentDays()), String.valueOf(r.getAbsentDays()),
                String.valueOf(r.getLeaveDays()), String.valueOf(r.getHalfDays()),
                String.valueOf(r.getOtherDays()), String.valueOf(r.getTotalRecords()),
                r.getPresentPercent() + "%"
        }).toList();
        return export("attendance-summary-" + startDate + "-" + endDate,
                "Attendance Summary — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> export(String filename, String title,
                                          String[] headers, List<String[]> data, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ReportExportUtil.pdfResponse(filename, ReportExportUtil.toPdf(title, headers, data));
        }
        return ReportExportUtil.csvResponse(filename, ReportExportUtil.toCsv(headers, data));
    }

    private String safe(Object val) {
        return val != null ? val.toString() : "—";
    }
}
