package com.feros.api.service;

import com.feros.api.dto.request.ApprovePayrollRequest;
import com.feros.api.dto.request.GeneratePayrollRequest;
import com.feros.api.dto.request.SalaryAdvanceRequest;
import com.feros.api.dto.response.PayrollResponse;
import com.feros.api.dto.response.SalaryAdvanceResponse;

import java.util.List;

public interface PayrollService {
    SalaryAdvanceResponse createAdvance(SalaryAdvanceRequest request);

    List<SalaryAdvanceResponse> getAllAdvances();

    List<SalaryAdvanceResponse> getAdvancesByUser(Long userId);

    List<SalaryAdvanceResponse> getPendingAdvancesByUser(Long userId);

    PayrollResponse generatePayroll(GeneratePayrollRequest request);

    PayrollResponse getPayrollById(Long id);

    List<PayrollResponse> getAllPayrolls();

    List<PayrollResponse> getPayrollsByUser(Long userId);

    PayrollResponse approvePayroll(Long id, ApprovePayrollRequest request);

    PayrollResponse cancelPayroll(Long id);
}