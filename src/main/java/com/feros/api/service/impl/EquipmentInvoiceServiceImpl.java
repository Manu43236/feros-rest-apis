package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentInvoiceItemRequest;
import com.feros.api.dto.request.EquipmentInvoiceRequest;
import com.feros.api.dto.response.EquipmentInvoiceItemResponse;
import com.feros.api.dto.response.EquipmentInvoicePrefillResponse;
import com.feros.api.dto.response.EquipmentInvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.EquipmentInvoiceItemType;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.enums.DailyLogStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentDailyLogRepository;
import com.feros.api.repository.EquipmentInvoiceRepository;
import com.feros.api.repository.MachineAssignmentRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.WorkOrderRepository;
import com.feros.api.service.EquipmentInvoiceService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentInvoiceServiceImpl implements EquipmentInvoiceService {

    private final EquipmentInvoiceRepository invoiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MachineAssignmentRepository assignmentRepository;
    private final EquipmentDailyLogRepository dailyLogRepository;
    private final TenantRepository tenantRepository;

    private Long tenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant tenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private WorkOrder workOrder(Long woId) {
        return workOrderRepository.findByIdAndTenantIdAndIsActiveTrue(woId, tenantId())
                .orElseThrow(() -> new FerosException("Work order not found", HttpStatus.NOT_FOUND));
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EquipmentInvoiceResponse create(Long woId, EquipmentInvoiceRequest req) {
        Tenant t = tenant();
        WorkOrder wo = workOrder(woId);

        EquipmentInvoice invoice = EquipmentInvoice.builder()
                .tenant(t)
                .workOrder(wo)
                .client(wo.getClient())
                .invoiceNumber(NumberUtil.generate(t.getPrefix(), t.getId(), NumberUtil.Type.EINV))
                .invoiceDate(req.getInvoiceDate())
                .dueDate(req.getDueDate())
                .billingPeriodStart(req.getBillingPeriodStart())
                .billingPeriodEnd(req.getBillingPeriodEnd())
                .taxPercent(req.getTaxPercent() != null ? req.getTaxPercent() : BigDecimal.ZERO)
                .notes(req.getNotes())
                .build();

        buildItems(invoice, req.getItems(), wo);
        computeTotals(invoice);

        return toResponse(invoiceRepository.save(invoice));
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    @Override
    public List<EquipmentInvoiceResponse> getByWorkOrder(Long woId) {
        workOrder(woId); // validates WO belongs to tenant
        return invoiceRepository.findByWorkOrderIdOrderByCreatedAtDesc(woId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public Page<EquipmentInvoiceResponse> getAll(int page, int size, EquipmentInvoiceStatus status) {
        return invoiceRepository.findAllPaged(tenantId(), status,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    @Override
    public EquipmentInvoiceResponse getById(Long id) {
        return toResponse(findById(id));
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EquipmentInvoiceResponse update(Long id, EquipmentInvoiceRequest req) {
        EquipmentInvoice invoice = findById(id);
        if (invoice.getStatus() != EquipmentInvoiceStatus.DRAFT) {
            throw new FerosException("Only DRAFT invoices can be edited", HttpStatus.BAD_REQUEST);
        }

        WorkOrder wo = invoice.getWorkOrder();
        invoice.setInvoiceDate(req.getInvoiceDate());
        invoice.setDueDate(req.getDueDate());
        invoice.setBillingPeriodStart(req.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(req.getBillingPeriodEnd());
        invoice.setTaxPercent(req.getTaxPercent() != null ? req.getTaxPercent() : BigDecimal.ZERO);
        invoice.setNotes(req.getNotes());

        invoice.getItems().clear();
        buildItems(invoice, req.getItems(), wo);
        computeTotals(invoice);

        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public EquipmentInvoiceResponse updateStatus(Long id, EquipmentInvoiceStatus newStatus) {
        EquipmentInvoice invoice = findById(id);
        invoice.setStatus(newStatus);
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        EquipmentInvoice invoice = findById(id);
        if (invoice.getStatus() != EquipmentInvoiceStatus.DRAFT) {
            throw new FerosException("Only DRAFT invoices can be deleted", HttpStatus.BAD_REQUEST);
        }
        invoiceRepository.delete(invoice);
    }

    // ── Prefill ────────────────────────────────────────────────────────────────

    @Override
    public List<EquipmentInvoicePrefillResponse> prefill(Long woId, LocalDate from, LocalDate to) {
        WorkOrder wo = workOrder(woId);

        List<MachineAssignment> assignments =
                assignmentRepository.findByWorkOrderIdOrderByStartDateAsc(woId);

        List<EquipmentDailyLog> logs = from != null && to != null
                ? dailyLogRepository.findByWorkOrderIdAndLogDateBetween(woId, from, to)
                : dailyLogRepository.findByWorkOrderIdOrderByLogDateAscIdAsc(woId);

        // WORKING only → hours (machine actually ran)
        // WORKING + IDLE → days (machine was on site and available; breakdown/no-machine days excluded)
        Map<Long, List<EquipmentDailyLog>> workingByAssignment = logs.stream()
                .filter(l -> l.getStatus() == DailyLogStatus.WORKING)
                .collect(Collectors.groupingBy(l -> l.getMachineAssignment().getId()));

        Map<Long, List<EquipmentDailyLog>> billableDaysByAssignment = logs.stream()
                .filter(l -> l.getStatus() == DailyLogStatus.WORKING || l.getStatus() == DailyLogStatus.IDLE)
                .collect(Collectors.groupingBy(l -> l.getMachineAssignment().getId()));

        BigDecimal periodMonths = computePeriodMonths(from, to, wo);

        List<EquipmentInvoicePrefillResponse> result = new ArrayList<>();
        for (MachineAssignment a : assignments) {
            List<EquipmentDailyLog> workingLogs  = workingByAssignment.getOrDefault(a.getId(), List.of());
            List<EquipmentDailyLog> billableLogs  = billableDaysByAssignment.getOrDefault(a.getId(), List.of());
            Equipment eq = a.getEquipment();

            BigDecimal totalHours = workingLogs.stream()
                    .map(l -> l.getHoursWorked() != null ? l.getHoursWorked() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Per-machine rate overrides WO rate
            BigDecimal effectiveRate = a.getRateAmount() != null ? a.getRateAmount() : wo.getRateAmount();
            String effectiveRateType = a.getRateType() != null ? a.getRateType().name() : wo.getRateType().name();

            result.add(EquipmentInvoicePrefillResponse.builder()
                    .machineAssignmentId(a.getId())
                    .serialNumber(eq.getSerialNumber())
                    .equipmentTypeName(eq.getEquipmentType().getName())
                    .suggestedHours(totalHours)
                    .suggestedDays(billableLogs.size())
                    .suggestedMonths(periodMonths)
                    .woRate(effectiveRate)
                    .woRateType(effectiveRateType)
                    .build());
        }
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private EquipmentInvoice findById(Long id) {
        return invoiceRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
    }

    private void buildItems(EquipmentInvoice invoice, List<EquipmentInvoiceItemRequest> itemRequests, WorkOrder wo) {
        int order = 0;
        for (EquipmentInvoiceItemRequest req : itemRequests) {
            String serialNumber = null;
            String typeName = null;

            if (req.getItemType() == EquipmentInvoiceItemType.MACHINE) {
                if (req.getMachineAssignmentId() == null) {
                    throw new FerosException("machineAssignmentId required for MACHINE items", HttpStatus.BAD_REQUEST);
                }
                MachineAssignment a = assignmentRepository.findByIdAndWorkOrderId(
                        req.getMachineAssignmentId(), wo.getId())
                        .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));
                Equipment eq = a.getEquipment();
                serialNumber = eq.getSerialNumber();
                typeName = eq.getEquipmentType().getName();
            }

            BigDecimal amount = req.getQuantity().multiply(req.getRate()).setScale(2, RoundingMode.HALF_UP);

            EquipmentInvoiceItem item = EquipmentInvoiceItem.builder()
                    .invoice(invoice)
                    .itemType(req.getItemType())
                    .description(req.getDescription())
                    .machineAssignmentId(req.getMachineAssignmentId())
                    .serialNumber(serialNumber)
                    .equipmentTypeName(typeName)
                    .billingType(req.getBillingType())
                    .quantity(req.getQuantity())
                    .rate(req.getRate())
                    .amount(amount)
                    .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : order)
                    .build();

            invoice.getItems().add(item);
            order++;
        }
    }

    private void computeTotals(EquipmentInvoice invoice) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(EquipmentInvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxPercent = invoice.getTaxPercent() != null ? invoice.getTaxPercent() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(subtotal.add(taxAmount));
    }

    private BigDecimal computePeriodMonths(LocalDate from, LocalDate to, WorkOrder wo) {
        LocalDate start = from != null ? from : wo.getStartDate();
        LocalDate end = to != null ? to : (wo.getEndDate() != null ? wo.getEndDate() : LocalDate.now());
        if (!end.isAfter(start)) return BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        return BigDecimal.valueOf(days).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
    }

    private EquipmentInvoiceResponse toResponse(EquipmentInvoice inv) {
        List<EquipmentInvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> EquipmentInvoiceItemResponse.builder()
                        .id(i.getId())
                        .itemType(i.getItemType())
                        .description(i.getDescription())
                        .machineAssignmentId(i.getMachineAssignmentId())
                        .serialNumber(i.getSerialNumber())
                        .equipmentTypeName(i.getEquipmentTypeName())
                        .billingType(i.getBillingType())
                        .quantity(i.getQuantity())
                        .rate(i.getRate())
                        .amount(i.getAmount())
                        .sortOrder(i.getSortOrder())
                        .build())
                .toList();

        WorkOrder wo = inv.getWorkOrder();
        Client client = inv.getClient();

        return EquipmentInvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .workOrderId(wo.getId())
                .woNumber(wo.getWoNumber())
                .clientId(client.getId())
                .clientName(client.getClientName())
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .billingPeriodStart(inv.getBillingPeriodStart())
                .billingPeriodEnd(inv.getBillingPeriodEnd())
                .status(inv.getStatus())
                .subtotal(inv.getSubtotal())
                .taxPercent(inv.getTaxPercent())
                .taxAmount(inv.getTaxAmount())
                .totalAmount(inv.getTotalAmount())
                .notes(inv.getNotes())
                .items(items)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
