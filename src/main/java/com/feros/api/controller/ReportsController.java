package com.feros.api.controller;

import com.feros.api.dto.response.*;
import com.feros.api.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/lr-register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<LrRegisterResponse>>> getLrRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "LR register fetched successfully",
                reportsService.getLrRegister(from, to, clientId)));
    }

    @GetMapping("/invoice-outstanding")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceOutstandingResponse>>> getInvoiceOutstanding(
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice outstanding report fetched successfully",
                reportsService.getInvoiceOutstanding(clientId)));
    }

    @GetMapping("/payroll-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<PayrollSummaryResponse>>> getPayrollSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll summary fetched successfully",
                reportsService.getPayrollSummary(from, to)));
    }

    @GetMapping("/collections")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<CollectionReportResponse>>> getCollectionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Collection report fetched successfully",
                reportsService.getCollectionReport(from, to, clientId)));
    }

    @GetMapping("/client-statement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ClientStatementResponse>> getClientStatement(
            @RequestParam Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Client statement fetched successfully",
                reportsService.getClientStatement(clientId, from, to)));
    }

    @GetMapping("/vehicle-trips")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleTripResponse>>> getVehicleTripReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle trip report fetched successfully",
                reportsService.getVehicleTripReport(from, to)));
    }

    @GetMapping("/order-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<OrderStatusResponse>>> getOrderStatusReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status report fetched successfully",
                reportsService.getOrderStatusReport(from, to, status)));
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceReportResponse>>> getAttendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance report fetched successfully",
                reportsService.getAttendanceReport(from, to)));
    }

    // ─── Section A — Daily Operations ─────────────────────────────────────────

    @GetMapping("/daily-vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<DailyVehicleActivityResponse>> getDailyVehicleActivity() {
        return ResponseEntity.ok(ApiResponse.success("Daily vehicle activity fetched", reportsService.getDailyVehicleActivity()));
    }

    @GetMapping("/local-long-trips")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<LocalLongTripSummaryResponse>> getLocalLongTripSummary() {
        return ResponseEntity.ok(ApiResponse.success("Local/long trip summary fetched", reportsService.getLocalLongTripSummary()));
    }

    @GetMapping("/idle-drivers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<IdleDriverResponse>>> getIdleDrivers() {
        return ResponseEntity.ok(ApiResponse.success("Idle drivers fetched", reportsService.getIdleDrivers()));
    }

    @GetMapping("/document-expiry")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DocumentExpiryAlertResponse>>> getDocumentExpiryAlerts(
            @RequestParam(defaultValue = "60") int daysAhead) {
        return ResponseEntity.ok(ApiResponse.success("Document expiry alerts fetched", reportsService.getDocumentExpiryAlerts(daysAhead)));
    }

    @GetMapping("/today-attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<TodayAttendanceSummaryResponse>> getTodayAttendance() {
        return ResponseEntity.ok(ApiResponse.success("Today's attendance fetched", reportsService.getTodayAttendance()));
    }

    @GetMapping("/delayed-trips")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DelayedTripResponse>>> getDelayedTrips() {
        return ResponseEntity.ok(ApiResponse.success("Delayed trips fetched", reportsService.getDelayedTrips()));
    }

    @GetMapping("/orders-backlog")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<OrdersBacklogResponse>>> getOrdersBacklog() {
        return ResponseEntity.ok(ApiResponse.success("Orders backlog fetched", reportsService.getOrdersBacklog()));
    }

    // ─── Section C — Orders & Assignments ─────────────────────────────────────

    @GetMapping("/order-fulfillment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderFulfillmentRateResponse>> getOrderFulfillmentRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Order fulfillment rate fetched", reportsService.getOrderFulfillmentRate(from, to)));
    }

    @GetMapping("/order-lead-time")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<OrderLeadTimeResponse>>> getOrderLeadTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Order lead time fetched", reportsService.getOrderLeadTime(from, to)));
    }

    @GetMapping("/unassigned-vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<UnassignedVehiclesResponse>>> getUnassignedVehicles() {
        return ResponseEntity.ok(ApiResponse.success("Unassigned vehicles fetched", reportsService.getUnassignedVehicles()));
    }

    @GetMapping("/driver-assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DriverAssignmentHistoryResponse>>> getDriverAssignmentHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Driver assignment history fetched", reportsService.getDriverAssignmentHistory(from, to)));
    }

    // ─── Section D — Trips & LRs ───────────────────────────────────────────────

    @GetMapping("/trips-in-progress")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TripInProgressResponse>>> getTripsInProgress() {
        return ResponseEntity.ok(ApiResponse.success("Trips in progress fetched", reportsService.getTripsInProgress()));
    }

    @GetMapping("/lr-status-funnel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<LrStatusFunnelResponse>> getLrStatusFunnel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("LR status funnel fetched", reportsService.getLrStatusFunnel(from, to)));
    }

    @GetMapping("/unbilled-lrs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<UnbilledLrResponse>>> getUnbilledLrs() {
        return ResponseEntity.ok(ApiResponse.success("Unbilled LRs fetched", reportsService.getUnbilledLrs()));
    }

    @GetMapping("/invoice-turnaround")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceTurnaroundResponse>>> getInvoiceTurnaround(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Invoice turnaround fetched", reportsService.getInvoiceTurnaround(from, to)));
    }

    @GetMapping("/trip-duration")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TripDurationResponse>>> getTripDurationAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Trip duration analysis fetched", reportsService.getTripDurationAnalysis(from, to)));
    }

    @GetMapping("/weight-variance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<WeightVarianceReportResponse>>> getWeightVarianceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Weight variance report fetched", reportsService.getWeightVarianceReport(from, to)));
    }

    @GetMapping("/overloading")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<OverloadingIncidentResponse>>> getOverloadingIncidents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Overloading incidents fetched", reportsService.getOverloadingIncidents(from, to)));
    }

    // ─── Section E — Vehicle Performance ──────────────────────────────────────

    @GetMapping("/vehicle-revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleRevenueResponse>>> getVehicleRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle revenue fetched", reportsService.getVehicleRevenue(from, to)));
    }

    @GetMapping("/vehicle-idle-days")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleIdleDaysResponse>>> getVehicleIdleDays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle idle days fetched", reportsService.getVehicleIdleDays(from, to)));
    }

    @GetMapping("/vehicle-trip-count")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleTripCountResponse>>> getVehicleTripCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle trip count fetched", reportsService.getVehicleTripCount(from, to)));
    }

    @GetMapping("/breakdown-frequency")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<BreakdownFrequencyResponse>>> getBreakdownFrequency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Breakdown frequency fetched", reportsService.getBreakdownFrequency(from, to)));
    }

    @GetMapping("/vehicle-service-cost")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleServiceCostResponse>>> getVehicleServiceCost(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle service cost fetched", reportsService.getVehicleServiceCost(from, to)));
    }

    // ─── Section F — Driver & Staff Performance ────────────────────────────────

    @GetMapping("/driver-performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<DriverPerformanceResponse>>> getDriverPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Driver performance fetched", reportsService.getDriverPerformance(from, to)));
    }

    @GetMapping("/attendance-gaps")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceGapsResponse>>> getAttendanceGaps(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Attendance gaps fetched", reportsService.getAttendanceGaps(from, to)));
    }

    @GetMapping("/attendance-trend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceTrendResponse>>> getAttendanceTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Attendance trend fetched", reportsService.getAttendanceTrend(from, to)));
    }

    @GetMapping("/attendance-calendar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<AttendanceCalendarResponse>> getAttendanceCalendar(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success("Attendance calendar fetched", reportsService.getAttendanceCalendar(year, month)));
    }

    // ─── Section G — Financial Intelligence ────────────────────────────────────

    @GetMapping("/invoice-aging")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<InvoiceAgingResponse>> getInvoiceAging() {
        return ResponseEntity.ok(ApiResponse.success("Invoice aging fetched", reportsService.getInvoiceAging()));
    }

    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<RevenueTrendResponse>>> getRevenueTrend() {
        return ResponseEntity.ok(ApiResponse.success("Revenue trend fetched", reportsService.getRevenueTrend()));
    }

    @GetMapping("/route-profitability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<RouteProfitabilityResponse>>> getRouteProfitability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Route profitability fetched", reportsService.getRouteProfitability(from, to)));
    }

    @GetMapping("/gst-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<GstSummaryResponse>>> getGstSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("GST summary fetched", reportsService.getGstSummary(from, to)));
    }

    @GetMapping("/credit-notes-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<CreditNoteSummaryResponse>>> getCreditNotesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Credit notes summary fetched", reportsService.getCreditNotesSummary(from, to)));
    }

    @GetMapping("/client-pending-billing")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ClientPendingBillingResponse>>> getClientPendingBilling() {
        return ResponseEntity.ok(ApiResponse.success("Client pending billing fetched", reportsService.getClientPendingBilling()));
    }

    // ─── Section H — Monthly Business Intelligence ─────────────────────────────

    @GetMapping("/top-clients")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TopClientResponse>>> getTopClients(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Top clients fetched", reportsService.getTopClients(from, to)));
    }

    @GetMapping("/top-materials")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TopMaterialResponse>>> getTopMaterials(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Top materials fetched", reportsService.getTopMaterials(from, to)));
    }

    @GetMapping("/top-routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TopRouteResponse>>> getTopRoutes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Top routes fetched", reportsService.getTopRoutes(from, to)));
    }

    @GetMapping("/on-time-delivery")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OnTimeDeliveryResponse>> getOnTimeDeliveryRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("On-time delivery rate fetched", reportsService.getOnTimeDeliveryRate(from, to)));
    }

    @GetMapping("/order-cancellation-rate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderCancellationRateResponse>> getOrderCancellationRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Order cancellation rate fetched", reportsService.getOrderCancellationRate(from, to)));
    }

    // ─── Section I — Inventory Reports ───────────────────────────────────────

    @GetMapping("/stock-levels")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getStockLevels() {
        return ResponseEntity.ok(ApiResponse.success("Stock levels fetched", reportsService.getStockLevels()));
    }

    @GetMapping("/stock-movement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Stock movement fetched", reportsService.getStockMovement(from, to)));
    }

    @GetMapping("/parts-by-vehicle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<VehiclePartConsumptionResponse>>> getPartsByVehicle(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Parts by vehicle fetched", reportsService.getPartsByVehicle(from, to)));
    }

    @GetMapping("/parts-by-type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<PartConsumptionByTypeResponse>>> getPartsByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Parts by type fetched", reportsService.getPartsByType(from, to)));
    }

    @GetMapping("/service-cost-breakdown")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ServiceCostBreakdownResponse>>> getServiceCostBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Service cost breakdown fetched", reportsService.getServiceCostBreakdown(from, to)));
    }

    // ─── Section J — Tyre Reports ─────────────────────────────────────────────

    @GetMapping("/tyres-by-vehicle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TyresByVehicleResponse>>> getTyresByVehicle() {
        return ResponseEntity.ok(ApiResponse.success("Tyres by vehicle fetched", reportsService.getTyresByVehicle()));
    }

    @GetMapping("/km-per-tyre")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<KmPerTyreResponse>>> getKmPerTyre() {
        return ResponseEntity.ok(ApiResponse.success("Km per tyre fetched", reportsService.getKmPerTyre()));
    }

    @GetMapping("/tyre-replacement-projection")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TyreReplacementProjectionResponse>>> getTyreReplacementProjection() {
        return ResponseEntity.ok(ApiResponse.success("Tyre replacement projection fetched", reportsService.getTyreReplacementProjection()));
    }

    @GetMapping("/tyre-cost-per-km")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<TyreCostPerKmResponse>>> getTyreCostPerKm() {
        return ResponseEntity.ok(ApiResponse.success("Tyre cost per km fetched", reportsService.getTyreCostPerKm()));
    }
}
