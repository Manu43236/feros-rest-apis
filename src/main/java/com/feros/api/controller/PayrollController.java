package com.feros.api.controller;

import com.feros.api.dto.request.ApprovePayrollRequest;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.dto.request.SalaryAdvanceRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.PayrollResponse;
import com.feros.api.dto.response.SalaryAdvanceResponse;
import com.feros.api.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

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
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.success(
                "Payrolls fetched successfully", payrollService.getAllPayrolls()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll fetched successfully", payrollService.getPayrollById(id)));
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
}