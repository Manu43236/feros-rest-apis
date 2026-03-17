package com.feros.api.service.impl;

import com.feros.api.dto.request.CreateInvoiceRequest;
import com.feros.api.dto.request.InvoicePaymentRequest;
import com.feros.api.dto.request.UpdateInvoiceStatusRequest;
import com.feros.api.dto.response.InvoiceLrResponse;
import com.feros.api.dto.response.InvoicePaymentResponse;
import com.feros.api.dto.response.InvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.InvoiceService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLrRepository invoiceLrRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final LrRepository lrRepository;
    private final LrChargeRepository lrChargeRepository;
    private final LrCheckpostRepository lrCheckpostRepository;
    private final UserRepository userRepository;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    private String generateInvoiceNumber(Tenant tenant) {
        return NumberUtil.generate(tenant.getPrefix(), tenant.getId(), NumberUtil.Type.INV);
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Long tenantId = getCurrentTenantId();
        Tenant tenant = getCurrentTenant();

        Client client = clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenantId)
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        // Validate all LRs belong to this client and not already invoiced
        List<Lr> lrs = new ArrayList<>();
        for (Long lrId : request.getLrIds()) {
            if (invoiceLrRepository.existsByLrIdAndIsActiveTrue(lrId)) {
                throw new FerosException("LR " + lrId + " is already invoiced", HttpStatus.CONFLICT);
            }
            Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenantId)
                    .orElseThrow(() -> new FerosException("LR " + lrId + " not found",
                            HttpStatus.NOT_FOUND));
            if (!lr.getOrder().getClient().getId().equals(client.getId())) {
                throw new FerosException("LR " + lrId + " does not belong to this client",
                        HttpStatus.BAD_REQUEST);
            }
            lrs.add(lr);
        }

        // Create invoice
        Invoice invoice = Invoice.builder()
                .tenant(tenant)
                .invoiceNumber(generateInvoiceNumber(tenant))
                .client(client)
                .invoiceDate(request.getInvoiceDate() != null ?
                        request.getInvoiceDate() : LocalDate.now())
                .dueDate(request.getDueDate())
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .advanceAdjusted(BigDecimal.ZERO)
                .creditNoteAdjusted(BigDecimal.ZERO)
                .amountPaid(BigDecimal.ZERO)
                .balanceDue(BigDecimal.ZERO)
                .invoiceStatus(InvoiceStatus.DRAFT)
                .remarks(request.getRemarks())
                .createdBy(getCurrentUser())
                .isActive(true)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create invoice LR items and calculate amounts
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Lr lr : lrs) {
            Order order = lr.getOrder();

            // Calculate freight amount
            BigDecimal freightAmount = calculateFreightAmount(lr, order);

            // Calculate LR charges total
            BigDecimal chargesAmount = lrChargeRepository.findByLrIdAndIsActiveTrue(lr.getId())
                    .stream()
                    .map(LrCharge::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate checkpost fines total
            BigDecimal checkpostFineAmount = lrCheckpostRepository
                    .findByLrIdAndIsActiveTrue(lr.getId())
                    .stream()
                    .map(c -> c.getFineAmount() != null ? c.getFineAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal lrTotal = freightAmount.add(chargesAmount).add(checkpostFineAmount);

            InvoiceLr invoiceLr = InvoiceLr.builder()
                    .tenant(tenant)
                    .invoice(savedInvoice)
                    .lr(lr)
                    .order(order)
                    .freightAmount(freightAmount)
                    .chargesAmount(chargesAmount)
                    .checkpostFineAmount(checkpostFineAmount)
                    .totalAmount(lrTotal)
                    .isActive(true)
                    .build();

            invoiceLrRepository.save(invoiceLr);
            subtotal = subtotal.add(lrTotal);
        }

        // Update invoice totals
        savedInvoice.setSubtotal(subtotal);
        savedInvoice.setTotalAmount(subtotal);
        savedInvoice.setBalanceDue(subtotal);
        invoiceRepository.save(savedInvoice);

        return mapToInvoiceResponse(savedInvoice);
    }

    private BigDecimal calculateFreightAmount(Lr lr, Order order) {
        BigDecimal billingWeight;
        if (order.getBillingOn() == BillingOn.DELIVERED_WEIGHT) {
            billingWeight = lr.getDeliveredWeight() != null ?
                    lr.getDeliveredWeight() : lr.getLoadedWeight();
        } else {
            billingWeight = lr.getLoadedWeight() != null ?
                    lr.getLoadedWeight() : lr.getAllocatedWeight();
        }

        if (billingWeight == null) billingWeight = lr.getAllocatedWeight();

        if (order.getFreightRateType() == FreightRateType.PER_TON) {
            return order.getFreightRate().multiply(billingWeight);
        } else if (order.getFreightRateType() == FreightRateType.PER_TRIP) {
            return order.getFreightRate();
        } else {
            // PER_KM — use route distance if available
            if (order.getRoute() != null && order.getRoute().getDistanceInKm() != null) {
                return order.getFreightRate().multiply(order.getRoute().getDistanceInKm());
            }
            return order.getFreightRate();
        }
    }

    @Override
    public InvoiceResponse getInvoiceById(Long id) {
        return mapToInvoiceResponse(invoiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToInvoiceResponse).toList();
    }

    @Override
    public List<InvoiceResponse> getInvoicesByClient(Long clientId) {
        return invoiceRepository
                .findByClientIdAndTenantIdAndIsActiveTrue(clientId, getCurrentTenantId())
                .stream().map(this::mapToInvoiceResponse).toList();
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));

        invoice.setInvoiceStatus(request.getInvoiceStatus());
        if (request.getRemarks() != null) invoice.setRemarks(request.getRemarks());

        if (request.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            invoice.setIsActive(false);
        }

        return mapToInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoicePaymentResponse recordPayment(Long id, InvoicePaymentRequest request) {
        Invoice invoice = invoiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));

        if (invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            throw new FerosException("Cannot record payment for cancelled invoice",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getAmount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new FerosException(
                    "Payment amount (" + request.getAmount() +
                    ") exceeds balance due (" + invoice.getBalanceDue() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        InvoicePayment payment = InvoicePayment.builder()
                .tenant(getCurrentTenant())
                .invoice(invoice)
                .paymentDate(request.getPaymentDate() != null ?
                        request.getPaymentDate() : LocalDate.now())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .createdBy(getCurrentUser())
                .isActive(true)
                .build();

        invoicePaymentRepository.save(payment);

        // Update invoice amounts
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(request.getAmount());
        BigDecimal newBalanceDue = invoice.getTotalAmount()
                .subtract(invoice.getAdvanceAdjusted())
                .subtract(invoice.getCreditNoteAdjusted())
                .subtract(newAmountPaid);

        invoice.setAmountPaid(newAmountPaid);
        invoice.setBalanceDue(newBalanceDue);

        if (newBalanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
        } else {
            invoice.setInvoiceStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        return mapToPaymentResponse(payment);
    }

    @Override
    public List<InvoicePaymentResponse> getPayments(Long id) {
        return invoicePaymentRepository.findByInvoiceIdAndIsActiveTrue(id)
                .stream().map(this::mapToPaymentResponse).toList();
    }

    // ===================== MAPPERS =====================
    private InvoiceResponse mapToInvoiceResponse(Invoice inv) {
        List<InvoiceLrResponse> lrItems = invoiceLrRepository
                .findByInvoiceIdAndIsActiveTrue(inv.getId())
                .stream().map(this::mapToInvoiceLrResponse).toList();

        List<InvoicePaymentResponse> payments = invoicePaymentRepository
                .findByInvoiceIdAndIsActiveTrue(inv.getId())
                .stream().map(this::mapToPaymentResponse).toList();

        return InvoiceResponse.builder()
                .id(inv.getId())
                .tenantId(inv.getTenant().getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .clientId(inv.getClient().getId())
                .clientName(inv.getClient().getClientName())
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .subtotal(inv.getSubtotal())
                .taxAmount(inv.getTaxAmount())
                .totalAmount(inv.getTotalAmount())
                .advanceAdjusted(inv.getAdvanceAdjusted())
                .creditNoteAdjusted(inv.getCreditNoteAdjusted())
                .amountPaid(inv.getAmountPaid())
                .balanceDue(inv.getBalanceDue())
                .invoiceStatus(inv.getInvoiceStatus())
                .remarks(inv.getRemarks())
                .lrItems(lrItems)
                .payments(payments)
                .createdById(inv.getCreatedBy().getId())
                .createdByName(inv.getCreatedBy().getName())
                .isActive(inv.getIsActive())
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    private InvoiceLrResponse mapToInvoiceLrResponse(InvoiceLr il) {
        return InvoiceLrResponse.builder()
                .id(il.getId())
                .lrId(il.getLr().getId())
                .lrNumber(il.getLr().getLrNumber())
                .orderId(il.getOrder().getId())
                .orderNumber(il.getOrder().getOrderNumber())
                .vehicleRegistrationNumber(
                        il.getLr().getVehicleAllocation().getVehicle().getRegistrationNumber())
                .freightAmount(il.getFreightAmount())
                .chargesAmount(il.getChargesAmount())
                .checkpostFineAmount(il.getCheckpostFineAmount())
                .totalAmount(il.getTotalAmount())
                .remarks(il.getRemarks())
                .createdAt(il.getCreatedAt())
                .build();
    }

    private InvoicePaymentResponse mapToPaymentResponse(InvoicePayment p) {
        return InvoicePaymentResponse.builder()
                .id(p.getId())
                .paymentDate(p.getPaymentDate())
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMode())
                .referenceNumber(p.getReferenceNumber())
                .remarks(p.getRemarks())
                .createdById(p.getCreatedBy().getId())
                .createdByName(p.getCreatedBy().getName())
                .createdAt(p.getCreatedAt())
                .build();
    }
}