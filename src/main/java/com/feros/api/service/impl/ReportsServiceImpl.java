package com.feros.api.service.impl;

import com.feros.api.dto.response.InvoiceOutstandingResponse;
import com.feros.api.dto.response.LrRegisterResponse;
import com.feros.api.dto.response.PayrollSummaryResponse;
import com.feros.api.entity.Invoice;
import com.feros.api.entity.Lr;
import com.feros.api.entity.Payroll;
import com.feros.api.repository.InvoiceRepository;
import com.feros.api.repository.LrRepository;
import com.feros.api.repository.PayrollRepository;
import com.feros.api.service.ReportsService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {

    private final LrRepository lrRepository;
    private final InvoiceRepository invoiceRepository;
    private final PayrollRepository payrollRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LrRegisterResponse> getLrRegister(LocalDate from, LocalDate to, Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = (clientId != null)
                ? lrRepository.findByTenantIdAndDateRangeAndClient(tenantId, from, to, clientId)
                : lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        return lrs.stream().map(l -> {
            var order = l.getOrder();
            var allocation = l.getVehicleAllocation();
            return LrRegisterResponse.builder()
                    .lrId(l.getId())
                    .lrNumber(l.getLrNumber())
                    .lrDate(l.getLrDate())
                    .orderNumber(order.getOrderNumber())
                    .clientId(order.getClient().getId())
                    .clientName(order.getClient().getClientName())
                    .vehicleRegistrationNumber(allocation.getVehicle().getRegistrationNumber())
                    .fromCity(order.getSourceCity().getName())
                    .fromState(order.getSourceState().getName())
                    .toCity(order.getDestinationCity().getName())
                    .toState(order.getDestinationState().getName())
                    .materialType(order.getMaterialType().getName())
                    .allocatedWeight(l.getAllocatedWeight())
                    .loadedWeight(l.getLoadedWeight())
                    .deliveredWeight(l.getDeliveredWeight())
                    .weightVariance(l.getWeightVariance())
                    .isOverloaded(l.getIsOverloaded())
                    .loadedAt(l.getLoadedAt())
                    .deliveredAt(l.getDeliveredAt())
                    .freightRateType(order.getFreightRateType().name())
                    .freightRate(order.getFreightRate())
                    .lrStatus(l.getLrStatus().name())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutstandingResponse> getInvoiceOutstanding(Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Invoice> invoices = (clientId != null)
                ? invoiceRepository.findOutstandingInvoicesByClient(tenantId, clientId)
                : invoiceRepository.findOutstandingInvoices(tenantId);

        LocalDate today = LocalDate.now();
        return invoices.stream().map(i -> {
            long daysOverdue = 0;
            if (i.getDueDate() != null && i.getDueDate().isBefore(today)) {
                daysOverdue = ChronoUnit.DAYS.between(i.getDueDate(), today);
            }
            return InvoiceOutstandingResponse.builder()
                    .invoiceId(i.getId())
                    .invoiceNumber(i.getInvoiceNumber())
                    .invoiceDate(i.getInvoiceDate())
                    .dueDate(i.getDueDate())
                    .clientId(i.getClient().getId())
                    .clientName(i.getClient().getClientName())
                    .totalAmount(i.getTotalAmount())
                    .amountPaid(i.getAmountPaid())
                    .balanceDue(i.getBalanceDue())
                    .invoiceStatus(i.getInvoiceStatus().name())
                    .daysOverdue(daysOverdue)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollSummaryResponse> getPayrollSummary(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Payroll> payrolls = payrollRepository.findByTenantIdAndDateRange(tenantId, from, to);

        return payrolls.stream().map(p -> {
            String roleName = p.getUser().getRoles().stream()
                    .findFirst().map(r -> r.getName().name()).orElse(null);
            return PayrollSummaryResponse.builder()
                    .payrollId(p.getId())
                    .userId(p.getUser().getId())
                    .userName(p.getUser().getName())
                    .userPhone(p.getUser().getPhone())
                    .roleName(roleName)
                    .payCycleStartDate(p.getPayCycleStartDate())
                    .payCycleEndDate(p.getPayCycleEndDate())
                    .totalDays(p.getTotalDays())
                    .presentDays(p.getPresentDays())
                    .absentDays(p.getAbsentDays())
                    .halfDays(p.getHalfDays())
                    .leaveDays(p.getLeaveDays())
                    .dailyRate(p.getDailyRate())
                    .basicPay(p.getBasicPay())
                    .overtimePay(p.getOvertimePay())
                    .tripBonus(p.getTripBonus())
                    .grossPay(p.getGrossPay())
                    .totalDeductions(p.getTotalDeductions())
                    .netPay(p.getNetPay())
                    .payrollStatus(p.getPayrollStatus().name())
                    .build();
        }).toList();
    }
}
