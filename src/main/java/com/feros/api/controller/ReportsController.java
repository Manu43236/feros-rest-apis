package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.InvoiceOutstandingResponse;
import com.feros.api.dto.response.LrRegisterResponse;
import com.feros.api.dto.response.PayrollSummaryResponse;
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

    // LR Register: all LRs in a date range, optionally filtered by client
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

    // Invoice Outstanding: all invoices with pending balance, optionally filtered by client
    @GetMapping("/invoice-outstanding")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceOutstandingResponse>>> getInvoiceOutstanding(
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice outstanding report fetched successfully",
                reportsService.getInvoiceOutstanding(clientId)));
    }

    // Payroll Summary: all payrolls whose cycle falls within the given date range
    @GetMapping("/payroll-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<PayrollSummaryResponse>>> getPayrollSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll summary fetched successfully",
                reportsService.getPayrollSummary(from, to)));
    }
}
