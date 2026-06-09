package com.feros.api.controller;

import com.feros.api.dto.request.ApprovePayrollRequest;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.dto.request.SalaryAdvanceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.PayrollResponse;
import com.feros.api.dto.response.SalaryAdvanceResponse;
import com.feros.api.service.PayrollService;
import com.feros.api.service.impl.PayslipPdfService;
import com.feros.api.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayslipPdfService payslipPdfService;

    // ===================== SALARY ADVANCES =====================
    @PostMapping("/advances")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> createAdvance(
            @Valid @RequestBody SalaryAdvanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Salary advance created successfully",
                payrollService.createAdvance(request)));
    }

    @GetMapping("/advances")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<SalaryAdvanceResponse>>> getAllAdvances() {
        return ResponseEntity.ok(ApiResponse.success(
                "Advances fetched successfully", payrollService.getAllAdvances()));
    }

    @GetMapping("/advances/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<SalaryAdvanceResponse>>> getAdvancesByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Advances fetched successfully", payrollService.getAdvancesByUser(userId)));
    }

    @GetMapping("/advances/user/{userId}/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<SalaryAdvanceResponse>>> getPendingAdvances(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending advances fetched successfully",
                payrollService.getPendingAdvancesByUser(userId)));
    }

    // ===================== PAYROLL =====================
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PayrollResponse>> generatePayroll(
            @Valid @RequestBody GeneratePayrollRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll generated successfully", payrollService.generatePayroll(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getAllPayrolls(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payrolls fetched successfully", payrollService.getAllPayrolls(page, size, search)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll fetched successfully", payrollService.getPayrollById(id)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('DRIVER', 'CLEANER', 'SUPERVISOR', 'SERVICE_MANAGER', 'TECHNICIAN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMyPayrolls() {
        return ResponseEntity.ok(ApiResponse.success(
                "Payrolls fetched successfully",
                payrollService.getPayrollsByUser(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payrolls fetched successfully", payrollService.getPayrollsByUser(userId)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PayrollResponse>> approvePayroll(
            @PathVariable Long id, @Valid @RequestBody ApprovePayrollRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll approved successfully", payrollService.approvePayroll(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PayrollResponse>> cancelPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll cancelled successfully", payrollService.cancelPayroll(id)));
    }

    @GetMapping("/{id}/payslip-pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'DRIVER', 'CLEANER', 'SUPERVISOR', 'SERVICE_MANAGER', 'TECHNICIAN', 'STORE_KEEPER')")
    public ResponseEntity<byte[]> getPayslipPdf(@PathVariable Long id) {
        byte[] pdf = payslipPdfService.generate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"payslip-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}