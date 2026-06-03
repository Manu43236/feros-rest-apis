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

    // ── LR Register ──────────────────────────────────────────────────────────────

    @GetMapping("/trips/lr-register")
    public ResponseEntity<ApiResponse<List<LrRegisterRow>>> getLrRegister(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "LR register fetched", reportService.getLrRegister(startDate, endDate, clientId)));
    }

    @GetMapping("/trips/lr-register/export")
    public ResponseEntity<byte[]> exportLrRegister(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) Long clientId,
            @RequestParam(defaultValue = "csv") String format) {
        List<LrRegisterRow> rows = reportService.getLrRegister(startDate, endDate, clientId);
        String[] headers = {"LR No.", "LR Date", "Order No.", "Client", "Vehicle", "Driver", "Cleaner",
                "From City", "From State", "To City", "To State", "Material",
                "Alloc. Wt (kg)", "Loaded Wt (kg)", "Delivered Wt (kg)", "Variance (kg)", "Overloaded",
                "Loaded At", "Delivered At", "E-Way Bill No.", "E-Way Date", "E-Way Valid Upto", "Status", "Remarks"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getLrNumber(), r.getLrDate().toString(), r.getOrderNumber(), r.getClientName(),
                r.getVehicleRegistrationNumber(), r.getDriverName(), r.getCleanerName(),
                r.getFromCity(), r.getFromState(), r.getToCity(), r.getToState(), r.getMaterialType(),
                safe(r.getAllocatedWeight()), safe(r.getLoadedWeight()), safe(r.getDeliveredWeight()),
                safe(r.getWeightVariance()), r.getIsOverloaded() != null && r.getIsOverloaded() ? "Yes" : "No",
                r.getLoadedAt() != null ? r.getLoadedAt().toString() : "—",
                r.getDeliveredAt() != null ? r.getDeliveredAt().toString() : "—",
                safe(r.getEwayBillNumber()), r.getEwayBillDate() != null ? r.getEwayBillDate().toString() : "—",
                r.getEwayBillValidUpto() != null ? r.getEwayBillValidUpto().toString() : "—",
                r.getLrStatus(), safe(r.getRemarks())
        }).toList();
        return export("lr-register-" + startDate + "-" + endDate,
                "LR Register — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Weight Discrepancy ────────────────────────────────────────────────────────

    @GetMapping("/trips/weight-discrepancy")
    public ResponseEntity<ApiResponse<List<WeightDiscrepancyRow>>> getWeightDiscrepancy(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Weight discrepancy fetched", reportService.getWeightDiscrepancy(startDate, endDate)));
    }

    @GetMapping("/trips/weight-discrepancy/export")
    public ResponseEntity<byte[]> exportWeightDiscrepancy(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<WeightDiscrepancyRow> rows = reportService.getWeightDiscrepancy(startDate, endDate);
        String[] headers = {"LR No.", "LR Date", "Client", "Vehicle", "From", "To", "Material",
                "Alloc. Wt (kg)", "Loaded Wt (kg)", "Delivered Wt (kg)", "Variance (kg)", "Overloaded", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getLrNumber(), r.getLrDate().toString(), r.getClientName(),
                r.getVehicleRegistrationNumber(), r.getFromCity(), r.getToCity(), r.getMaterialType(),
                safe(r.getAllocatedWeight()), safe(r.getLoadedWeight()), safe(r.getDeliveredWeight()),
                safe(r.getWeightVariance()), r.getIsOverloaded() != null && r.getIsOverloaded() ? "Yes" : "No",
                r.getLrStatus()
        }).toList();
        return export("weight-discrepancy-" + startDate + "-" + endDate,
                "Weight Discrepancy — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Delayed Deliveries ────────────────────────────────────────────────────────

    @GetMapping("/trips/delayed-deliveries")
    public ResponseEntity<ApiResponse<List<DelayedDeliveryRow>>> getDelayedDeliveries(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "3") int thresholdDays) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delayed deliveries fetched", reportService.getDelayedDeliveries(startDate, endDate, thresholdDays)));
    }

    @GetMapping("/trips/delayed-deliveries/export")
    public ResponseEntity<byte[]> exportDelayedDeliveries(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "3") int thresholdDays,
            @RequestParam(defaultValue = "csv") String format) {
        List<DelayedDeliveryRow> rows = reportService.getDelayedDeliveries(startDate, endDate, thresholdDays);
        String[] headers = {"LR No.", "LR Date", "Client", "Vehicle", "Driver", "From", "To", "Material", "Loaded At", "Days in Transit", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getLrNumber(), r.getLrDate().toString(), r.getClientName(),
                r.getVehicleRegistrationNumber(), r.getDriverName(),
                r.getFromCity(), r.getToCity(), r.getMaterialType(),
                r.getLoadedAt().toString(), String.valueOf(r.getDaysInTransit()), r.getLrStatus()
        }).toList();
        return export("delayed-deliveries-" + startDate + "-" + endDate,
                "Delayed Deliveries — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Order Register ────────────────────────────────────────────────────────────

    @GetMapping("/orders/register")
    public ResponseEntity<ApiResponse<List<OrderRegisterRow>>> getOrderRegister(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order register fetched", reportService.getOrderRegister(startDate, endDate, status)));
    }

    @GetMapping("/orders/register/export")
    public ResponseEntity<byte[]> exportOrderRegister(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "csv") String format) {
        List<OrderRegisterRow> rows = reportService.getOrderRegister(startDate, endDate, status);
        String[] headers = {"Order No.", "Order Date", "Exp. Delivery", "Client", "Material",
                "From", "To", "Total Wt (kg)", "Fulfilled Wt (kg)", "Freight Rate Type",
                "Freight Rate", "Total Freight", "Status", "Payment Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getOrderNumber(), r.getOrderDate().toString(),
                r.getExpectedDeliveryDate() != null ? r.getExpectedDeliveryDate().toString() : "—",
                r.getClientName(), r.getMaterialType(),
                r.getFromCity() + ", " + r.getFromState(), r.getToCity() + ", " + r.getToState(),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()),
                r.getFreightRateType(), safe(r.getFreightRate()), safe(r.getTotalFreightAmount()),
                r.getOrderStatus(), r.getOrderPaymentStatus()
        }).toList();
        return export("order-register-" + startDate + "-" + endDate,
                "Order Register — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Open Orders ───────────────────────────────────────────────────────────────

    @GetMapping("/orders/open")
    public ResponseEntity<ApiResponse<List<OpenOrderRow>>> getOpenOrders() {
        return ResponseEntity.ok(ApiResponse.success("Open orders fetched", reportService.getOpenOrders()));
    }

    @GetMapping("/orders/open/export")
    public ResponseEntity<byte[]> exportOpenOrders(@RequestParam(defaultValue = "csv") String format) {
        List<OpenOrderRow> rows = reportService.getOpenOrders();
        String[] headers = {"Order No.", "Order Date", "Exp. Delivery", "Client", "Material",
                "From", "To", "Total Wt (kg)", "Fulfilled Wt (kg)", "Pending Wt (kg)", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getOrderNumber(), r.getOrderDate().toString(),
                r.getExpectedDeliveryDate() != null ? r.getExpectedDeliveryDate().toString() : "—",
                r.getClientName(), r.getMaterialType(), r.getFromCity(), r.getToCity(),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()), safe(r.getPendingWeight()),
                r.getOrderStatus()
        }).toList();
        return export("open-orders", "Open Orders", headers, data, format);
    }

    // ── Order Client Summary ──────────────────────────────────────────────────────

    @GetMapping("/orders/client-summary")
    public ResponseEntity<ApiResponse<List<OrderClientSummaryRow>>> getOrderClientSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order client summary fetched", reportService.getOrderClientSummary(startDate, endDate)));
    }

    @GetMapping("/orders/client-summary/export")
    public ResponseEntity<byte[]> exportOrderClientSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<OrderClientSummaryRow> rows = reportService.getOrderClientSummary(startDate, endDate);
        String[] headers = {"Client", "Total Orders", "Completed", "In Progress", "Cancelled",
                "Total Wt (kg)", "Fulfilled Wt (kg)", "Total Freight"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getClientName(), String.valueOf(r.getTotalOrders()),
                String.valueOf(r.getCompletedOrders()), String.valueOf(r.getInProgressOrders()),
                String.valueOf(r.getCancelledOrders()),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()), safe(r.getTotalFreightAmount())
        }).toList();
        return export("order-client-summary-" + startDate + "-" + endDate,
                "Order Client Summary — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Overdue Orders ────────────────────────────────────────────────────────────

    @GetMapping("/orders/overdue")
    public ResponseEntity<ApiResponse<List<OverdueOrderRow>>> getOverdueOrders(
            @RequestParam(defaultValue = "1") int thresholdDays) {
        return ResponseEntity.ok(ApiResponse.success(
                "Overdue orders fetched", reportService.getOverdueOrders(thresholdDays)));
    }

    @GetMapping("/orders/overdue/export")
    public ResponseEntity<byte[]> exportOverdueOrders(
            @RequestParam(defaultValue = "1") int thresholdDays,
            @RequestParam(defaultValue = "csv") String format) {
        List<OverdueOrderRow> rows = reportService.getOverdueOrders(thresholdDays);
        String[] headers = {"Order No.", "Order Date", "Exp. Delivery", "Days Overdue",
                "Client", "Material", "From", "To", "Total Wt (kg)", "Fulfilled Wt (kg)", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getOrderNumber(), r.getOrderDate().toString(),
                r.getExpectedDeliveryDate().toString(), String.valueOf(r.getDaysOverdue()),
                r.getClientName(), r.getMaterialType(), r.getFromCity(), r.getToCity(),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()), r.getOrderStatus()
        }).toList();
        return export("overdue-orders", "Overdue Orders", headers, data, format);
    }

    // ── Weight Fulfillment ────────────────────────────────────────────────────────

    @GetMapping("/orders/weight-fulfillment")
    public ResponseEntity<ApiResponse<List<WeightFulfillmentRow>>> getWeightFulfillment(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Weight fulfillment fetched", reportService.getWeightFulfillment(startDate, endDate)));
    }

    @GetMapping("/orders/weight-fulfillment/export")
    public ResponseEntity<byte[]> exportWeightFulfillment(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<WeightFulfillmentRow> rows = reportService.getWeightFulfillment(startDate, endDate);
        String[] headers = {"Order No.", "Order Date", "Client", "Material", "From", "To",
                "Total Wt (kg)", "Fulfilled Wt (kg)", "Pending Wt (kg)", "Fulfillment %", "Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getOrderNumber(), r.getOrderDate().toString(), r.getClientName(),
                r.getMaterialType(), r.getFromCity(), r.getToCity(),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()),
                safe(r.getPendingWeight()), r.getFulfillmentPercent() + "%", r.getOrderStatus()
        }).toList();
        return export("weight-fulfillment-" + startDate + "-" + endDate,
                "Weight Fulfillment — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Route-wise Order Summary ──────────────────────────────────────────────────

    @GetMapping("/orders/route-summary")
    public ResponseEntity<ApiResponse<List<OrderRouteSummaryRow>>> getOrderRouteSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order route summary fetched", reportService.getOrderRouteSummary(startDate, endDate)));
    }

    @GetMapping("/orders/route-summary/export")
    public ResponseEntity<byte[]> exportOrderRouteSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<OrderRouteSummaryRow> rows = reportService.getOrderRouteSummary(startDate, endDate);
        String[] headers = {"From City", "From State", "To City", "To State",
                "Total Orders", "Completed", "Total Wt (kg)", "Fulfilled Wt (kg)", "Total Freight"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getFromCity(), r.getFromState(), r.getToCity(), r.getToState(),
                String.valueOf(r.getTotalOrders()), String.valueOf(r.getCompletedOrders()),
                safe(r.getTotalWeight()), safe(r.getTotalWeightFulfilled()), safe(r.getTotalFreightAmount())
        }).toList();
        return export("order-route-summary-" + startDate + "-" + endDate,
                "Order Route Summary — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Order Payment Status ──────────────────────────────────────────────────────

    @GetMapping("/orders/payment-status")
    public ResponseEntity<ApiResponse<List<OrderPaymentStatusRow>>> getOrderPaymentStatus(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) String paymentStatus) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order payment status fetched", reportService.getOrderPaymentStatus(startDate, endDate, paymentStatus)));
    }

    @GetMapping("/orders/payment-status/export")
    public ResponseEntity<byte[]> exportOrderPaymentStatus(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "csv") String format) {
        List<OrderPaymentStatusRow> rows = reportService.getOrderPaymentStatus(startDate, endDate, paymentStatus);
        String[] headers = {"Order No.", "Order Date", "Client", "Total Freight", "Order Status", "Payment Status"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getOrderNumber(), r.getOrderDate().toString(), r.getClientName(),
                safe(r.getTotalFreightAmount()), r.getOrderStatus(), r.getOrderPaymentStatus()
        }).toList();
        return export("order-payment-status-" + startDate + "-" + endDate,
                "Order Payment Status — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Vehicle Trip Summary ──────────────────────────────────────────────────────

    @GetMapping("/trips/vehicle-summary")
    public ResponseEntity<ApiResponse<List<VehicleTripSummaryRow>>> getVehicleTripSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle trip summary fetched", reportService.getVehicleTripSummary(startDate, endDate)));
    }

    @GetMapping("/trips/vehicle-summary/export")
    public ResponseEntity<byte[]> exportVehicleTripSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<VehicleTripSummaryRow> rows = reportService.getVehicleTripSummary(startDate, endDate);
        String[] headers = {"Vehicle No.", "Type", "Total Trips", "Completed", "In Transit", "Cancelled",
                "Total Alloc. Wt (kg)", "Total Loaded Wt (kg)", "Total Delivered Wt (kg)"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getRegistrationNumber(), r.getVehicleType(),
                String.valueOf(r.getTotalTrips()), String.valueOf(r.getCompletedTrips()),
                String.valueOf(r.getInTransitTrips()), String.valueOf(r.getCancelledTrips()),
                safe(r.getTotalAllocatedWeight()), safe(r.getTotalLoadedWeight()), safe(r.getTotalDeliveredWeight())
        }).toList();
        return export("vehicle-trip-summary-" + startDate + "-" + endDate,
                "Vehicle Trip Summary — " + startDate + " to " + endDate, headers, data, format);
    }

    // ── Client Trip Summary ───────────────────────────────────────────────────────

    @GetMapping("/trips/client-summary")
    public ResponseEntity<ApiResponse<List<ClientTripSummaryRow>>> getClientTripSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client trip summary fetched", reportService.getClientTripSummary(startDate, endDate)));
    }

    @GetMapping("/trips/client-summary/export")
    public ResponseEntity<byte[]> exportClientTripSummary(
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        List<ClientTripSummaryRow> rows = reportService.getClientTripSummary(startDate, endDate);
        String[] headers = {"Client", "Total Trips", "Completed", "In Transit", "Cancelled",
                "Total Alloc. Wt (kg)", "Total Loaded Wt (kg)", "Total Delivered Wt (kg)"};
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.getClientName(),
                String.valueOf(r.getTotalTrips()), String.valueOf(r.getCompletedTrips()),
                String.valueOf(r.getInTransitTrips()), String.valueOf(r.getCancelledTrips()),
                safe(r.getTotalAllocatedWeight()), safe(r.getTotalLoadedWeight()), safe(r.getTotalDeliveredWeight())
        }).toList();
        return export("client-trip-summary-" + startDate + "-" + endDate,
                "Client Trip Summary — " + startDate + " to " + endDate, headers, data, format);
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
