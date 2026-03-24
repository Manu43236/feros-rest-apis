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
}
