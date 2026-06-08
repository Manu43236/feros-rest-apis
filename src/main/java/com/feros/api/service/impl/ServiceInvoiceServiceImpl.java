package com.feros.api.service.impl;

import com.feros.api.dto.response.ServiceInvoiceResponse;
import com.feros.api.entity.ServiceInvoice;
import com.feros.api.entity.ServicePart;
import com.feros.api.entity.SparePartsTransaction;
import com.feros.api.enums.ServiceInvoiceStatus;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.ServiceInvoiceRepository;
import com.feros.api.repository.ServicePartRepository;
import com.feros.api.repository.SparePartsTransactionRepository;
import com.feros.api.service.ServiceInvoiceService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceInvoiceServiceImpl implements ServiceInvoiceService {

    private final ServiceInvoiceRepository invoiceRepository;
    private final ServicePartRepository servicePartRepository;
    private final SparePartsTransactionRepository transactionRepository;
    private final ServiceInvoicePdfService pdfService;

    @Override
    public ServiceInvoiceResponse getByServiceId(Long serviceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceInvoice inv = invoiceRepository.findByServiceIdAndIsActiveTrue(serviceId)
                .orElseThrow(() -> new FerosException("Invoice not found for this service", HttpStatus.NOT_FOUND));
        if (!inv.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Access denied", HttpStatus.FORBIDDEN);
        }
        return mapToResponse(inv);
    }

    @Override
    public ServiceInvoiceResponse getById(Long invoiceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceInvoice inv = invoiceRepository.findByIdAndTenantIdAndIsActiveTrue(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        return mapToResponse(inv);
    }

    @Override
    public List<ServiceInvoiceResponse> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return invoiceRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public ServiceInvoiceResponse updateVendorAmount(Long invoiceId, BigDecimal vendorAmount, String vendorInvoiceNo) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceInvoice inv = invoiceRepository.findByIdAndTenantIdAndIsActiveTrue(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        if (inv.getInvoiceType() != com.feros.api.enums.ServiceInvoiceType.EXTERNAL) {
            throw new FerosException("Vendor amount can only be updated for OEM/3rd party services", HttpStatus.BAD_REQUEST);
        }
        if (vendorAmount != null) inv.setVendorAmount(vendorAmount);
        if (vendorInvoiceNo != null && !vendorInvoiceNo.isBlank()) inv.setVendorInvoiceNo(vendorInvoiceNo);
        // For external invoices totalAmount = vendorAmount
        if (inv.getVendorAmount() != null) inv.setTotalAmount(inv.getVendorAmount());
        return mapToResponse(invoiceRepository.save(inv));
    }

    @Override
    @Transactional
    public ServiceInvoiceResponse markPaid(Long invoiceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceInvoice inv = invoiceRepository.findByIdAndTenantIdAndIsActiveTrue(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        if (inv.getPaymentStatus() == ServiceInvoiceStatus.PAID) {
            throw new FerosException("Invoice is already marked as paid", HttpStatus.BAD_REQUEST);
        }
        inv.setPaymentStatus(ServiceInvoiceStatus.PAID);
        inv.setPaidAt(TimeUtil.nowIst());
        return mapToResponse(invoiceRepository.save(inv));
    }

    @Override
    public byte[] generatePdf(Long invoiceId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceInvoice inv = invoiceRepository.findByIdAndTenantIdAndIsActiveTrue(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        return pdfService.generate(inv, servicePartRepository.findByServiceIdOrderByCreatedAtDesc(inv.getService().getId()));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────
    private ServiceInvoiceResponse mapToResponse(ServiceInvoice inv) {
        var svc = inv.getService();
        List<ServicePart> parts = servicePartRepository.findByServiceIdOrderByCreatedAtDesc(svc.getId())
                .stream().filter(p -> p.getStatus() == ServicePartStatus.APPROVED).toList();

        // Build a map of servicePart.id → transaction for cost lookup (avoids N+1)
        List<Long> partIds = parts.stream().map(ServicePart::getId).toList();
        Map<Long, SparePartsTransaction> txByPartId = partIds.isEmpty() ? Map.of() :
                transactionRepository.findByServicePart_IdIn(partIds).stream()
                        .collect(Collectors.toMap(tx -> tx.getServicePart().getId(), tx -> tx, (a, b) -> a));

        List<ServiceInvoiceResponse.TaskLineItem> taskItems = svc.getTasks().stream()
                .map(t -> ServiceInvoiceResponse.TaskLineItem.builder()
                        .name(t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName())
                        .cost(t.getCost() != null ? t.getCost() : BigDecimal.ZERO)
                        .build())
                .toList();

        List<ServiceInvoiceResponse.PartLineItem> partItems = parts.stream()
                .map(p -> {
                    SparePartsTransaction tx = txByPartId.get(p.getId());
                    BigDecimal unitCost = tx != null && tx.getUnitCost() != null ? tx.getUnitCost() : BigDecimal.ZERO;
                    BigDecimal totalCost = tx != null && tx.getTotalCost() != null ? tx.getTotalCost() : BigDecimal.ZERO;
                    return ServiceInvoiceResponse.PartLineItem.builder()
                            .partName(p.getSparePart().getName())
                            .partNumber(p.getSparePart().getPartNumber())
                            .unit(p.getSparePart().getUnit())
                            .quantity(p.getQuantityApproved() != null ? p.getQuantityApproved() : p.getQuantityRequested())
                            .unitCost(unitCost)
                            .totalCost(totalCost)
                            .build();
                })
                .toList();

        return ServiceInvoiceResponse.builder()
                .id(inv.getId())
                .tenantId(inv.getTenant().getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .invoiceType(inv.getInvoiceType())
                .serviceId(svc.getId())
                .serviceNumber(svc.getServiceNumber())
                .vehicleRegistrationNumber(svc.getVehicle().getRegistrationNumber())
                .serviceType(svc.getServiceType())
                .vendorName(svc.getVendorName())
                .serviceDate(svc.getServiceDate())
                .completedDate(svc.getCompletedDate())
                .tasks(taskItems)
                .parts(partItems)
                .tasksTotal(inv.getTasksTotal())
                .partsTotal(inv.getPartsTotal())
                .labourCharges(inv.getLabourCharges())
                .subTotal(inv.getSubTotal())
                .gstRate(inv.getGstRate())
                .gstAmount(inv.getGstAmount())
                .totalAmount(inv.getTotalAmount())
                .vendorAmount(inv.getVendorAmount())
                .vendorInvoiceNo(inv.getVendorInvoiceNo())
                .paymentStatus(inv.getPaymentStatus())
                .paidAt(inv.getPaidAt())
                .paidByName(inv.getPaidBy() != null ? inv.getPaidBy().getName() : null)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
