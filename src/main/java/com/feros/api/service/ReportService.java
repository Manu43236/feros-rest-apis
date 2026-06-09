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

    List<ClientVehiclePnlRow> getClientVehiclePnl(LocalDate startDate, LocalDate endDate);

    List<TripPnlRow> getTripPnl(LocalDate startDate, LocalDate endDate);

    // ── Inventory Reports ──────────────────────────────────────────────────────
    List<StockSummaryRow> getStockSummary();
    List<StockInwardRow> getStockInward(LocalDate startDate, LocalDate endDate);
    List<StockOutwardRow> getStockOutward(LocalDate startDate, LocalDate endDate);
    List<PartRequestRow> getPartRequests(LocalDate startDate, LocalDate endDate);
    List<ConsumptionByVehicleRow> getConsumptionByVehicle(LocalDate startDate, LocalDate endDate);

    // ── Tyre Reports ───────────────────────────────────────────────────────────
    List<TyreInventoryRow> getTyreInventory();
    List<TyreFittingRow> getTyreFittingRegister(LocalDate startDate, LocalDate endDate);
    List<TyreRemovalRow> getTyreRemovalRegister(LocalDate startDate, LocalDate endDate);
    List<TyreLifeRow> getTyreLifeReport();
    List<TyreRequestRow> getTyreRequests(LocalDate startDate, LocalDate endDate);
    List<TyreRotationRow> getTyreRotationLog(LocalDate startDate, LocalDate endDate);

    // ── Payroll Reports ────────────────────────────────────────────────────────
    List<PayrollSummaryReportRow> getPayrollSummary(LocalDate startDate, LocalDate endDate);
    List<SalaryRegisterRow> getSalaryRegister(LocalDate startDate, LocalDate endDate);
    List<AdvanceRegisterRow> getAdvanceRegister(LocalDate startDate, LocalDate endDate);
    List<PayrollByRoleRow> getPayrollByRole(LocalDate startDate, LocalDate endDate);
    List<PayrollYtdRow> getPayrollYtd(int year);

    // ── Mechanic Performance ───────────────────────────────────────────────────
    List<TechnicianPerformanceRow> getTechnicianPerformance(LocalDate startDate, LocalDate endDate);
}
