package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentAdvanceRequest;
import com.feros.api.dto.request.EquipmentPaymentRequest;
import com.feros.api.dto.request.EquipmentRetentionReleaseRequest;
import com.feros.api.dto.response.EquipmentAdvanceResponse;
import com.feros.api.dto.response.EquipmentPaymentResponse;
import com.feros.api.dto.response.EquipmentRetentionReleaseResponse;
import com.feros.api.dto.response.WoReceivablesSummaryResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.EquipmentReceivablesService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentReceivablesServiceImpl implements EquipmentReceivablesService {

    private final TenantRepository tenantRepository;
    private final WorkOrderRepository workOrderRepository;
    private final EquipmentInvoiceRepository invoiceRepository;
    private final EquipmentPaymentRepository paymentRepository;
    private final EquipmentAdvanceRepository advanceRepository;
    private final EquipmentRetentionReleaseRepository releaseRepository;

    private Long tenantId() { return SecurityUtil.getCurrentTenantId(); }

    private Tenant tenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private WorkOrder workOrder(Long woId) {
        return workOrderRepository.findByIdAndTenantIdAndIsActiveTrue(woId, tenantId())
                .orElseThrow(() -> new FerosException("Work order not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentInvoice invoice(Long invId) {
        return invoiceRepository.findByIdAndTenantId(invId, tenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
    }

    // ── Payments ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EquipmentPaymentResponse recordPayment(Long woId, Long invId, EquipmentPaymentRequest req) {
        workOrder(woId);
        EquipmentInvoice inv = invoice(invId);

        EquipmentPayment payment = EquipmentPayment.builder()
                .tenant(tenant())
                .invoice(inv)
                .workOrderId(woId)
                .clientId(inv.getClient().getId())
                .amount(req.getAmount())
                .paymentDate(req.getPaymentDate())
                .paymentMode(req.getPaymentMode())
                .utrReference(req.getUtrReference())
                .notes(req.getNotes())
                .build();
        paymentRepository.save(payment);

        updateInvoiceStatus(inv);
        return toPaymentResponse(payment, inv);
    }

    @Override
    public List<EquipmentPaymentResponse> listPayments(Long woId, Long invId) {
        workOrder(woId);
        EquipmentInvoice inv = invoice(invId);
        return paymentRepository.findByInvoiceIdAndTenantId(invId, tenantId())
                .stream().map(p -> toPaymentResponse(p, inv)).toList();
    }

    @Override
    @Transactional
    public void deletePayment(Long woId, Long invId, Long payId) {
        workOrder(woId);
        EquipmentInvoice inv = invoice(invId);
        EquipmentPayment payment = paymentRepository.findByIdAndTenantId(payId, tenantId())
                .orElseThrow(() -> new FerosException("Payment not found", HttpStatus.NOT_FOUND));
        paymentRepository.delete(payment);
        updateInvoiceStatus(inv);
    }

    private void updateInvoiceStatus(EquipmentInvoice inv) {
        if (inv.getStatus() == EquipmentInvoiceStatus.DRAFT || inv.getStatus() == EquipmentInvoiceStatus.CANCELLED)
            return;
        BigDecimal received = paymentRepository.sumByInvoice(inv.getId(), tenantId());
        BigDecimal total = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
        if (received.compareTo(total) >= 0) {
            inv.setStatus(EquipmentInvoiceStatus.PAID);
        } else if (received.compareTo(BigDecimal.ZERO) > 0) {
            inv.setStatus(EquipmentInvoiceStatus.PARTIALLY_PAID);
        } else {
            inv.setStatus(EquipmentInvoiceStatus.SENT);
        }
        invoiceRepository.save(inv);
    }

    // ── Advances ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EquipmentAdvanceResponse recordAdvance(Long woId, EquipmentAdvanceRequest req) {
        WorkOrder wo = workOrder(woId);
        EquipmentAdvance advance = EquipmentAdvance.builder()
                .tenant(tenant())
                .workOrder(wo)
                .clientId(wo.getClient() != null ? wo.getClient().getId() : null)
                .amount(req.getAmount())
                .paymentDate(req.getPaymentDate())
                .paymentMode(req.getPaymentMode())
                .utrReference(req.getUtrReference())
                .notes(req.getNotes())
                .build();
        return toAdvanceResponse(advanceRepository.save(advance));
    }

    @Override
    public List<EquipmentAdvanceResponse> listAdvances(Long woId) {
        workOrder(woId);
        return advanceRepository.findByWorkOrderIdAndTenantId(woId, tenantId())
                .stream().map(this::toAdvanceResponse).toList();
    }

    @Override
    @Transactional
    public void deleteAdvance(Long woId, Long advId) {
        workOrder(woId);
        EquipmentAdvance advance = advanceRepository.findByIdAndTenantId(advId, tenantId())
                .orElseThrow(() -> new FerosException("Advance not found", HttpStatus.NOT_FOUND));
        advanceRepository.delete(advance);
    }

    // ── Retention Releases ────────────────────────────────────────────────────

    @Override
    @Transactional
    public EquipmentRetentionReleaseResponse recordRetentionRelease(Long woId, EquipmentRetentionReleaseRequest req) {
        WorkOrder wo = workOrder(woId);
        EquipmentRetentionRelease release = EquipmentRetentionRelease.builder()
                .tenant(tenant())
                .workOrder(wo)
                .clientId(wo.getClient() != null ? wo.getClient().getId() : null)
                .amount(req.getAmount())
                .releaseDate(req.getReleaseDate())
                .notes(req.getNotes())
                .build();
        return toReleaseResponse(releaseRepository.save(release));
    }

    @Override
    public List<EquipmentRetentionReleaseResponse> listRetentionReleases(Long woId) {
        workOrder(woId);
        return releaseRepository.findByWorkOrderIdAndTenantId(woId, tenantId())
                .stream().map(this::toReleaseResponse).toList();
    }

    @Override
    @Transactional
    public void deleteRetentionRelease(Long woId, Long relId) {
        workOrder(woId);
        EquipmentRetentionRelease release = releaseRepository.findByIdAndTenantId(relId, tenantId())
                .orElseThrow(() -> new FerosException("Retention release not found", HttpStatus.NOT_FOUND));
        releaseRepository.delete(release);
    }

    // ── Reconciliation Summary ────────────────────────────────────────────────

    @Override
    public WoReceivablesSummaryResponse getSummary(Long woId) {
        workOrder(woId);
        Long tid = tenantId();

        List<EquipmentInvoice> invoices = invoiceRepository.findByWorkOrderViaItems(tid, woId)
                .stream()
                .filter(i -> i.getStatus() != EquipmentInvoiceStatus.DRAFT && i.getStatus() != EquipmentInvoiceStatus.CANCELLED)
                .toList();

        BigDecimal grossBilled = BigDecimal.ZERO;
        BigDecimal totalRetentionHeldOnInvoices = BigDecimal.ZERO;

        List<WoReceivablesSummaryResponse.InvoiceReceivableRow> rows = invoices.stream().map(inv -> {
            BigDecimal invTotal = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal retPct = inv.getRetentionPercent() != null ? inv.getRetentionPercent() : BigDecimal.ZERO;
            BigDecimal retOnInvoice = invTotal.multiply(retPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal received = paymentRepository.sumByInvoice(inv.getId(), tid);
            List<EquipmentPaymentResponse> payments = paymentRepository.findByInvoiceIdAndTenantId(inv.getId(), tid)
                    .stream().map(p -> toPaymentResponse(p, inv)).toList();
            return WoReceivablesSummaryResponse.InvoiceReceivableRow.builder()
                    .invoiceId(inv.getId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .invoiceDate(inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : null)
                    .totalAmount(invTotal)
                    .retentionOnInvoice(retOnInvoice)
                    .totalReceived(received)
                    .balanceDue(invTotal.subtract(received))
                    .status(inv.getStatus().name())
                    .payments(payments)
                    .build();
        }).toList();

        for (WoReceivablesSummaryResponse.InvoiceReceivableRow r : rows) {
            grossBilled = grossBilled.add(r.getTotalAmount());
            totalRetentionHeldOnInvoices = totalRetentionHeldOnInvoices.add(r.getRetentionOnInvoice());
        }

        BigDecimal totalReceived = paymentRepository.sumByWorkOrder(woId, tid);
        BigDecimal totalReleased = releaseRepository.sumByWorkOrder(woId, tid);
        BigDecimal retentionHeld = totalRetentionHeldOnInvoices.subtract(totalReleased).max(BigDecimal.ZERO);
        BigDecimal totalAdvances = advanceRepository.sumByWorkOrder(woId, tid);
        BigDecimal balanceDue = grossBilled.subtract(totalReceived).subtract(retentionHeld).subtract(totalAdvances);

        List<EquipmentAdvanceResponse> advances = advanceRepository.findByWorkOrderIdAndTenantId(woId, tid)
                .stream().map(this::toAdvanceResponse).toList();
        List<EquipmentRetentionReleaseResponse> releases = releaseRepository.findByWorkOrderIdAndTenantId(woId, tid)
                .stream().map(this::toReleaseResponse).toList();

        return WoReceivablesSummaryResponse.builder()
                .grossBilled(grossBilled)
                .totalReceived(totalReceived)
                .retentionHeld(retentionHeld)
                .totalRetentionReleased(totalReleased)
                .totalAdvances(totalAdvances)
                .balanceDue(balanceDue)
                .invoices(rows)
                .advances(advances)
                .retentionReleases(releases)
                .build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private EquipmentPaymentResponse toPaymentResponse(EquipmentPayment p, EquipmentInvoice inv) {
        return EquipmentPaymentResponse.builder()
                .id(p.getId())
                .invoiceId(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .workOrderId(p.getWorkOrderId())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMode(p.getPaymentMode())
                .utrReference(p.getUtrReference())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private EquipmentAdvanceResponse toAdvanceResponse(EquipmentAdvance a) {
        return EquipmentAdvanceResponse.builder()
                .id(a.getId())
                .workOrderId(a.getWorkOrder().getId())
                .amount(a.getAmount())
                .paymentDate(a.getPaymentDate())
                .paymentMode(a.getPaymentMode())
                .utrReference(a.getUtrReference())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private EquipmentRetentionReleaseResponse toReleaseResponse(EquipmentRetentionRelease r) {
        return EquipmentRetentionReleaseResponse.builder()
                .id(r.getId())
                .workOrderId(r.getWorkOrder().getId())
                .amount(r.getAmount())
                .releaseDate(r.getReleaseDate())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
