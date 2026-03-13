package com.feros.api.service;

import com.feros.api.dto.response.InvoiceOutstandingResponse;
import com.feros.api.dto.response.LrRegisterResponse;
import com.feros.api.dto.response.PayrollSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportsService {
    List<LrRegisterResponse> getLrRegister(LocalDate from, LocalDate to, Long clientId);
    List<InvoiceOutstandingResponse> getInvoiceOutstanding(Long clientId);
    List<PayrollSummaryResponse> getPayrollSummary(LocalDate from, LocalDate to);
}
