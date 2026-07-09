package com.feros.api.service.impl;

import com.feros.api.dto.request.LeaseInvoiceItemRequest;
import com.feros.api.dto.request.LeaseInvoiceRequest;
import com.feros.api.dto.response.LeaseInvoiceItemResponse;
import com.feros.api.dto.response.LeaseInvoicePrefillResponse;
import com.feros.api.dto.response.LeaseInvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.enums.RateType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.LeaseInvoiceRepository;
import com.feros.api.repository.LeaseVehicleAssignmentRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.VehicleLeaseRepository;
import com.feros.api.service.LeaseInvoiceService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseInvoiceServiceImpl implements LeaseInvoiceService {

    private final LeaseInvoiceRepository invoiceRepository;
    private final VehicleLeaseRepository leaseRepository;
    private final LeaseVehicleAssignmentRepository assignmentRepository;
    private final TenantRepository tenantRepository;

    private Long tenantId() { return SecurityUtil.getCurrentTenantId(); }

    private Tenant tenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private VehicleLease fetchLease(Long leaseId) {
        return leaseRepository.findByIdAndTenantIdAndIsActiveTrue(leaseId, tenantId())
                .orElseThrow(() -> new FerosException("Lease not found", HttpStatus.NOT_FOUND));
    }

    // ── Get All ───────────────────────────────────────────────────────────────

    @Override
    public List<LeaseInvoiceResponse> getAll() {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Prefill ───────────────────────────────────────────────────────────────

    @Override
    public List<LeaseInvoicePrefillResponse> prefill(Long leaseId, LocalDate from, LocalDate to) {
        VehicleLease lease = fetchLease(leaseId);
        List<LeaseVehicleAssignment> assignments =
                assignmentRepository.findByLeaseIdOrderByStartDateAsc(leaseId);

        return assignments.stream()
                .filter(a -> !a.getStartDate().isAfter(to)
                        && (a.getEndDate() == null || !a.getEndDate().isBefore(from)))
                .map(a -> {
                    LocalDate start = a.getStartDate().isBefore(from) ? from : a.getStartDate();
                    LocalDate end = (a.getEndDate() == null || a.getEndDate().isAfter(to)) ? to : a.getEndDate();
                    long days = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
                    BigDecimal amount = calculateAmount(lease.getRateType(), days, a.getRatePerVehicle());
                    String vehicleType = a.getVehicle().getVehicleType() != null
                            ? a.getVehicle().getVehicleType().getName() : null;

                    return LeaseInvoicePrefillResponse.builder()
                            .assignmentId(a.getId())
                            .registrationNumber(a.getVehicle().getRegistrationNumber())
                            .vehicleType(vehicleType)
                            .assignmentStart(a.getStartDate())
                            .assignmentEnd(a.getEndDate())
                            .daysInPeriod((int) days)
                            .rate(a.getRatePerVehicle())
                            .suggestedAmount(amount)
                            .rateType(lease.getRateType().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaseInvoiceResponse create(Long leaseId, LeaseInvoiceRequest req) {
        if (req.getBillingPeriodEnd().isAfter(LocalDate.now()))
            throw new FerosException("Billing period end cannot be in the future", HttpStatus.BAD_REQUEST);
        if (req.getBillingPeriodEnd().isBefore(req.getBillingPeriodStart()))
            throw new FerosException("Billing period end must be after start", HttpStatus.BAD_REQUEST);

        VehicleLease lease = fetchLease(leaseId);
        Tenant t = tenant();

        LeaseInvoice invoice = LeaseInvoice.builder()
                .tenant(t)
                .lease(lease)
                .client(lease.getClient())
                .invoiceNumber(NumberUtil.generate(t.getPrefix(), t.getId(), NumberUtil.Type.LSEINV))
                .invoiceDate(req.getInvoiceDate())
                .dueDate(req.getDueDate())
                .billingPeriodStart(req.getBillingPeriodStart())
                .billingPeriodEnd(req.getBillingPeriodEnd())
                .cgstPercentage(nvl(req.getCgstPercentage()))
                .sgstPercentage(nvl(req.getSgstPercentage()))
                .igstPercentage(nvl(req.getIgstPercentage()))
                .notes(req.getNotes())
                .build();

        int sortOrder = 0;
        for (LeaseInvoiceItemRequest itemReq : req.getItems()) {
            BigDecimal amount = itemReq.getAmount() != null
                    ? itemReq.getAmount().setScale(2, RoundingMode.HALF_UP)
                    : (itemReq.getRate() != null && itemReq.getDays() != null
                            ? itemReq.getRate().multiply(BigDecimal.valueOf(itemReq.getDays())).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);

            invoice.getItems().add(LeaseInvoiceItem.builder()
                    .invoice(invoice)
                    .leaseVehicleAssignmentId(itemReq.getLeaseVehicleAssignmentId())
                    .registrationNumber(itemReq.getRegistrationNumber())
                    .description(itemReq.getDescription())
                    .days(itemReq.getDays())
                    .rate(itemReq.getRate())
                    .amount(amount)
                    .sortOrder(sortOrder)
                    .build());
            sortOrder++;
        }

        computeTotals(invoice);
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public List<LeaseInvoiceResponse> getByLease(Long leaseId) {
        fetchLease(leaseId);
        return invoiceRepository.findByLease_IdAndTenantIdOrderByCreatedAtDesc(leaseId, tenantId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public LeaseInvoiceResponse getById(Long id) {
        return toResponse(findById(id));
    }

    // ── Update status ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaseInvoiceResponse updateStatus(Long id, EquipmentInvoiceStatus newStatus) {
        LeaseInvoice invoice = findById(id);
        invoice.setStatus(newStatus);
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        LeaseInvoice invoice = findById(id);
        if (invoice.getStatus() != EquipmentInvoiceStatus.DRAFT)
            throw new FerosException("Only DRAFT invoices can be deleted", HttpStatus.BAD_REQUEST);
        invoiceRepository.delete(invoice);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LeaseInvoice findById(Long id) {
        return invoiceRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
    }

    private void computeTotals(LeaseInvoice invoice) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(LeaseInvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        BigDecimal cgstAmt = subtotal.multiply(invoice.getCgstPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal sgstAmt = subtotal.multiply(invoice.getSgstPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal igstAmt = subtotal.multiply(invoice.getIgstPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmt = cgstAmt.add(sgstAmt).add(igstAmt);

        invoice.setSubtotal(subtotal);
        invoice.setCgstAmount(cgstAmt);
        invoice.setSgstAmount(sgstAmt);
        invoice.setIgstAmount(igstAmt);
        invoice.setTaxAmount(taxAmt);
        invoice.setTotalAmount(subtotal.add(taxAmt));
    }

    private BigDecimal calculateAmount(RateType rateType, long days, BigDecimal rate) {
        if (rateType == RateType.MONTHLY) {
            return rate.multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        }
        return rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private LeaseInvoiceResponse toResponse(LeaseInvoice inv) {
        Client client = inv.getClient();
        Tenant tenant = inv.getTenant();
        VehicleLease lease = inv.getLease();

        List<LeaseInvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> LeaseInvoiceItemResponse.builder()
                        .id(i.getId())
                        .leaseVehicleAssignmentId(i.getLeaseVehicleAssignmentId())
                        .registrationNumber(i.getRegistrationNumber())
                        .description(i.getDescription())
                        .days(i.getDays())
                        .rate(i.getRate())
                        .amount(i.getAmount())
                        .sortOrder(i.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return LeaseInvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .leaseId(lease.getId())
                .leaseNumber(lease.getLeaseNumber())
                .clientId(client.getId())
                .clientName(client.getClientName())
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .billingPeriodStart(inv.getBillingPeriodStart())
                .billingPeriodEnd(inv.getBillingPeriodEnd())
                .status(inv.getStatus())
                .subtotal(inv.getSubtotal())
                .cgstPercentage(inv.getCgstPercentage())
                .sgstPercentage(inv.getSgstPercentage())
                .igstPercentage(inv.getIgstPercentage())
                .cgstAmount(inv.getCgstAmount())
                .sgstAmount(inv.getSgstAmount())
                .igstAmount(inv.getIgstAmount())
                .taxAmount(inv.getTaxAmount())
                .totalAmount(inv.getTotalAmount())
                .notes(inv.getNotes())
                .items(items)
                .tenantName(tenant.getCompanyName())
                .tenantGstin(tenant.getGstin())
                .tenantAddress(tenant.getAddress())
                .tenantState(tenant.getState())
                .clientGstin(client.getGstin())
                .clientAddress(client.getAddress())
                .clientStateName(client.getState() != null ? client.getState().getName() : null)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
