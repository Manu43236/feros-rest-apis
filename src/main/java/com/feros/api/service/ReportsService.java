package com.feros.api.service;

import com.feros.api.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportsService {
    List<LrRegisterResponse> getLrRegister(LocalDate from, LocalDate to, Long clientId);
    List<InvoiceOutstandingResponse> getInvoiceOutstanding(Long clientId);
    List<PayrollSummaryResponse> getPayrollSummary(LocalDate from, LocalDate to);
    List<CollectionReportResponse> getCollectionReport(LocalDate from, LocalDate to, Long clientId);
    ClientStatementResponse getClientStatement(Long clientId, LocalDate from, LocalDate to);
    List<VehicleTripResponse> getVehicleTripReport(LocalDate from, LocalDate to);
    List<OrderStatusResponse> getOrderStatusReport(LocalDate from, LocalDate to, String status);
    List<AttendanceReportResponse> getAttendanceReport(LocalDate from, LocalDate to);

    // Section A — Daily Operations
    DailyVehicleActivityResponse getDailyVehicleActivity();
    LocalLongTripSummaryResponse getLocalLongTripSummary();
    List<IdleDriverResponse> getIdleDrivers();
    List<DocumentExpiryAlertResponse> getDocumentExpiryAlerts(int daysAhead);
    TodayAttendanceSummaryResponse getTodayAttendance();
    List<DelayedTripResponse> getDelayedTrips();
    List<OrdersBacklogResponse> getOrdersBacklog();

    // Section C — Orders & Assignments
    OrderFulfillmentRateResponse getOrderFulfillmentRate(LocalDate from, LocalDate to);
    List<OrderLeadTimeResponse> getOrderLeadTime(LocalDate from, LocalDate to);
    List<UnassignedVehiclesResponse> getUnassignedVehicles();
    List<DriverAssignmentHistoryResponse> getDriverAssignmentHistory(LocalDate from, LocalDate to);

    // Section D — Trips & LRs
    List<TripInProgressResponse> getTripsInProgress();
    LrStatusFunnelResponse getLrStatusFunnel(LocalDate from, LocalDate to);
    List<UnbilledLrResponse> getUnbilledLrs();
    List<InvoiceTurnaroundResponse> getInvoiceTurnaround(LocalDate from, LocalDate to);
    List<TripDurationResponse> getTripDurationAnalysis(LocalDate from, LocalDate to);
    List<WeightVarianceReportResponse> getWeightVarianceReport(LocalDate from, LocalDate to);
    List<OverloadingIncidentResponse> getOverloadingIncidents(LocalDate from, LocalDate to);

    // Section E — Vehicle Performance
    List<VehicleRevenueResponse> getVehicleRevenue(LocalDate from, LocalDate to);
    List<VehicleIdleDaysResponse> getVehicleIdleDays(LocalDate from, LocalDate to);
    List<VehicleTripCountResponse> getVehicleTripCount(LocalDate from, LocalDate to);
    List<BreakdownFrequencyResponse> getBreakdownFrequency(LocalDate from, LocalDate to);
    List<VehicleServiceCostResponse> getVehicleServiceCost(LocalDate from, LocalDate to);

    // Section F — Driver & Staff Performance
    List<DriverPerformanceResponse> getDriverPerformance(LocalDate from, LocalDate to);
    List<AttendanceGapsResponse> getAttendanceGaps(LocalDate from, LocalDate to);
    List<AttendanceTrendResponse> getAttendanceTrend(LocalDate from, LocalDate to);
    AttendanceCalendarResponse getAttendanceCalendar(int year, int month);

    // Section G — Financial Intelligence
    InvoiceAgingResponse getInvoiceAging();
    List<RevenueTrendResponse> getRevenueTrend();
    List<RouteProfitabilityResponse> getRouteProfitability(LocalDate from, LocalDate to);
    List<GstSummaryResponse> getGstSummary(LocalDate from, LocalDate to);
    List<CreditNoteSummaryResponse> getCreditNotesSummary(LocalDate from, LocalDate to);
    List<ClientPendingBillingResponse> getClientPendingBilling();

    // Section H — Monthly Business Intelligence
    List<TopClientResponse> getTopClients(LocalDate from, LocalDate to);
    List<TopMaterialResponse> getTopMaterials(LocalDate from, LocalDate to);
    List<TopRouteResponse> getTopRoutes(LocalDate from, LocalDate to);
    OnTimeDeliveryResponse getOnTimeDeliveryRate(LocalDate from, LocalDate to);
    OrderCancellationRateResponse getOrderCancellationRate(LocalDate from, LocalDate to);

    // Section I — Inventory Reports
    List<StockLevelResponse> getStockLevels();
    List<StockMovementResponse> getStockMovement(LocalDate from, LocalDate to);
    List<VehiclePartConsumptionResponse> getPartsByVehicle(LocalDate from, LocalDate to);
    List<PartConsumptionByTypeResponse> getPartsByType(LocalDate from, LocalDate to);
    List<ServiceCostBreakdownResponse> getServiceCostBreakdown(LocalDate from, LocalDate to);

    // Section J — Tire Reports
    List<TiresByVehicleResponse> getTiresByVehicle();
    List<KmPerTireResponse> getKmPerTire();
    List<TireReplacementProjectionResponse> getTireReplacementProjection();
    List<TireCostPerKmResponse> getTireCostPerKm();
}
