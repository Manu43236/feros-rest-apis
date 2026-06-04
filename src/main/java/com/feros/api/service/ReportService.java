package com.feros.api.service;

import com.feros.api.dto.response.report.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<FleetStatusRow> getFleetStatus(LocalDate date);

List<FuelMileageRow> getFuelMileage(LocalDate startDate, LocalDate endDate);

    List<BreakdownReportRow> getBreakdowns(LocalDate startDate, LocalDate endDate);

    List<DocumentExpiryRow> getDocumentExpiry(int days);

    List<MaintenanceServiceRow> getMaintenanceService(LocalDate startDate, LocalDate endDate);

    List<AttendanceDailyRow> getAttendanceDaily(LocalDate startDate, LocalDate endDate);

    List<AttendanceSummaryRow> getAttendanceSummary(LocalDate startDate, LocalDate endDate);

    List<LrRegisterRow> getLrRegister(LocalDate startDate, LocalDate endDate, Long clientId);

    List<WeightDiscrepancyRow> getWeightDiscrepancy(LocalDate startDate, LocalDate endDate);

    List<DelayedDeliveryRow> getDelayedDeliveries(LocalDate startDate, LocalDate endDate, int thresholdDays);

    List<VehicleTripSummaryRow> getVehicleTripSummary(LocalDate startDate, LocalDate endDate);

    List<ClientTripSummaryRow> getClientTripSummary(LocalDate startDate, LocalDate endDate);

    List<OrderRegisterRow> getOrderRegister(LocalDate startDate, LocalDate endDate, String status);

    List<OpenOrderRow> getOpenOrders();

    List<OrderClientSummaryRow> getOrderClientSummary(LocalDate startDate, LocalDate endDate);

    List<OverdueOrderRow> getOverdueOrders(int thresholdDays);

    List<WeightFulfillmentRow> getWeightFulfillment(LocalDate startDate, LocalDate endDate);

    List<OrderRouteSummaryRow> getOrderRouteSummary(LocalDate startDate, LocalDate endDate);

    List<OrderPaymentStatusRow> getOrderPaymentStatus(LocalDate startDate, LocalDate endDate, String paymentStatus);

    List<InvoiceRegisterRow> getInvoiceRegister(LocalDate startDate, LocalDate endDate, String status);

    List<OutstandingInvoiceRow> getOutstandingInvoices();

    List<InvoiceAgingRow> getInvoiceAging();

    List<CollectionRow> getCollections(LocalDate startDate, LocalDate endDate);

    List<CreditNoteRegisterRow> getCreditNoteRegister(LocalDate startDate, LocalDate endDate);

    List<TripExpenseReportRow> getTripExpenses(LocalDate startDate, LocalDate endDate);

    List<FuelCostRow> getFuelCostSummary(LocalDate startDate, LocalDate endDate);

    List<MaintenanceCostRow> getMaintenanceCostSummary(LocalDate startDate, LocalDate endDate);

    List<DocumentCostRow> getDocumentCostSummary(LocalDate startDate, LocalDate endDate);

    List<DriverPerformanceRow> getDriverPerformance(LocalDate startDate, LocalDate endDate);

    List<CleanerPerformanceRow> getCleanerPerformance(LocalDate startDate, LocalDate endDate);

    PnlSummaryRow getPnlSummary(LocalDate startDate, LocalDate endDate);

    List<ClientPnlRow> getClientPnl(LocalDate startDate, LocalDate endDate);

    List<VehiclePnlRow> getVehiclePnl(LocalDate startDate, LocalDate endDate);

    List<RoutePnlRow> getRoutePnl(LocalDate startDate, LocalDate endDate);
}
