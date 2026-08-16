package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.CreateInvoiceRequest;
import com.feros.api.dto.request.InvoicePaymentRequest;
import com.feros.api.dto.request.UpdateInvoiceRequest;
import com.feros.api.dto.request.UpdateInvoiceStatusRequest;
import com.feros.api.dto.response.InvoiceLrResponse;
import com.feros.api.dto.response.InvoicePaymentResponse;
import com.feros.api.dto.response.InvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.exception.FerosException;
import com.feros.api.entity.master.TenantSettings;
import com.feros.api.repository.*;
import com.feros.api.service.InvoiceService;
import com.feros.api.service.NotificationService;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.S3Service;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final TenantSettingsRepository tenantSettingsRepository;
    private final S3Service s3Service;
    private final NumberGeneratorService numberGenerator;
    private final NotificationService notificationService;

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
        return numberGenerator.generateFY(tenant.getId(), NumberUtil.Type.INV);
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
                        request.getInvoiceDate() : TimeUtil.today())
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

        // Apply tax: either IGST (inter-state) or CGST+SGST (intra-state)
        BigDecimal igstPct = request.getIgstPercentage() != null
                ? request.getIgstPercentage() : BigDecimal.ZERO;
        BigDecimal cgstPct = BigDecimal.ZERO;
        BigDecimal sgstPct = BigDecimal.ZERO;
        BigDecimal cgstAmt = BigDecimal.ZERO;
        BigDecimal sgstAmt = BigDecimal.ZERO;
        BigDecimal igstAmt = BigDecimal.ZERO;

        if (igstPct.compareTo(BigDecimal.ZERO) > 0) {
            // Inter-state: IGST only
            igstAmt = subtotal.multiply(igstPct)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        } else {
            // Intra-state: CGST + SGST
            cgstPct = request.getCgstPercentage() != null ? request.getCgstPercentage() : BigDecimal.ZERO;
            sgstPct = request.getSgstPercentage() != null ? request.getSgstPercentage() : BigDecimal.ZERO;
            cgstAmt = subtotal.multiply(cgstPct)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            sgstAmt = subtotal.multiply(sgstPct)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
        BigDecimal taxAmt = cgstAmt.add(sgstAmt).add(igstAmt);
        BigDecimal total  = subtotal.add(taxAmt);

        savedInvoice.setSubtotal(subtotal);
        savedInvoice.setCgstPercentage(cgstPct);
        savedInvoice.setSgstPercentage(sgstPct);
        savedInvoice.setIgstPercentage(igstPct);
        savedInvoice.setCgstAmount(cgstAmt);
        savedInvoice.setSgstAmount(sgstAmt);
        savedInvoice.setIgstAmount(igstAmt);
        savedInvoice.setTaxAmount(taxAmt);
        savedInvoice.setTotalAmount(total);
        savedInvoice.setBalanceDue(total);
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
    public Page<InvoiceResponse> getAllInvoices(int page, int size, String search, InvoiceStatus status) {
        Long tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return invoiceRepository.findAllPaged(tenantId, status, searchParam, pageable)
                .map(this::mapToInvoiceResponse);
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
            // Release LRs so they can be re-invoiced
            List<InvoiceLr> lrLinks = invoiceLrRepository.findByInvoiceIdAndIsActiveTrue(invoice.getId());
            lrLinks.forEach(link -> link.setIsActive(false));
            invoiceLrRepository.saveAll(lrLinks);
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

        if (request.getPaymentMode() == com.feros.api.enums.PaymentMode.OTHER &&
                (request.getPaymentModeLabel() == null || request.getPaymentModeLabel().isBlank())) {
            throw new FerosException("Please describe the payment mode when selecting 'Other'", HttpStatus.BAD_REQUEST);
        }

        InvoicePayment payment = InvoicePayment.builder()
                .tenant(getCurrentTenant())
                .invoice(invoice)
                .paymentDate(request.getPaymentDate() != null ?
                        request.getPaymentDate() : TimeUtil.today())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .paymentModeLabel(request.getPaymentMode() == com.feros.api.enums.PaymentMode.OTHER
                        ? request.getPaymentModeLabel().trim() : null)
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

        String clientName = invoice.getClient().getClientName();
        String amountStr = "₹" + String.format("%,.0f", request.getAmount());
        String balanceStr = newBalanceDue.compareTo(BigDecimal.ZERO) <= 0
                ? "Fully paid." : "Balance: ₹" + String.format("%,.0f", newBalanceDue) + " remaining.";
        notificationService.sendToRoles(invoice.getTenant(),
                List.of(RoleName.ADMIN, RoleName.OFFICE_STAFF),
                NotificationType.INVOICE_PAYMENT_RECEIVED,
                "Payment Received — " + clientName,
                invoice.getInvoiceNumber() + " | " + clientName + " | " + amountStr + " received. " + balanceStr,
                Map.of("type", "INVOICE_PAYMENT"));

        return mapToPaymentResponse(payment);
    }

    @Override
    public List<InvoicePaymentResponse> getPayments(Long id) {
        return invoicePaymentRepository.findByInvoiceIdAndIsActiveTrue(id)
                .stream().map(this::mapToPaymentResponse).toList();
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));

        if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
        if (request.getRemarks() != null) invoice.setRemarks(request.getRemarks());

        return mapToInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public void deletePayment(Long invoiceId, Long paymentId) {
        Invoice invoice = invoiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(invoiceId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));

        InvoicePayment payment = invoicePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new FerosException("Payment not found", HttpStatus.NOT_FOUND));

        if (!payment.getInvoice().getId().equals(invoiceId)) {
            throw new FerosException("Payment does not belong to this invoice", HttpStatus.BAD_REQUEST);
        }

        payment.setIsActive(false);
        invoicePaymentRepository.save(payment);

        BigDecimal newAmountPaid = invoice.getAmountPaid().subtract(payment.getAmount());
        BigDecimal newBalanceDue = invoice.getTotalAmount()
                .subtract(invoice.getAdvanceAdjusted())
                .subtract(invoice.getCreditNoteAdjusted())
                .subtract(newAmountPaid);

        invoice.setAmountPaid(newAmountPaid);
        invoice.setBalanceDue(newBalanceDue);

        if (newBalanceDue.compareTo(invoice.getTotalAmount()) >= 0) {
            if (invoice.getInvoiceStatus() == InvoiceStatus.PAID
                    || invoice.getInvoiceStatus() == InvoiceStatus.PARTIALLY_PAID) {
                invoice.setInvoiceStatus(InvoiceStatus.SENT);
            }
        } else if (newBalanceDue.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setInvoiceStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
    }

    @Override
    public List<Long> getInvoicedLrIds() {
        return invoiceLrRepository.findActiveLrIds(getCurrentTenantId());
    }

    // ===================== MAPPERS =====================
    private InvoiceResponse mapToInvoiceResponse(Invoice inv) {
        List<InvoiceLrResponse> lrItems = invoiceLrRepository
                .findByInvoiceIdAndIsActiveTrue(inv.getId())
                .stream().map(this::mapToInvoiceLrResponse).toList();

        List<InvoicePaymentResponse> payments = invoicePaymentRepository
                .findByInvoiceIdAndIsActiveTrue(inv.getId())
                .stream().map(this::mapToPaymentResponse).toList();

        com.feros.api.entity.Tenant t  = inv.getTenant();
        com.feros.api.entity.Client  cl = inv.getClient();
        TenantSettings settings = tenantSettingsRepository.findByTenantId(t.getId()).orElse(null);

        return InvoiceResponse.builder()
                .id(inv.getId())
                .tenantId(t.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .clientId(cl.getId())
                .clientName(cl.getClientName())
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .subtotal(inv.getSubtotal())
                .cgstPercentage(inv.getCgstPercentage())
                .sgstPercentage(inv.getSgstPercentage())
                .igstPercentage(inv.getIgstPercentage())
                .cgstAmount(inv.getCgstAmount())
                .sgstAmount(inv.getSgstAmount())
                .igstAmount(inv.getIgstAmount())
                .taxAmount(inv.getTaxAmount())
                // Tenant print details
                .tenantCompanyName(t.getCompanyName())
                .tenantLogoUrl(t.getLogoUrl() != null ? s3Service.getPublicUrl(t.getLogoUrl()) : null)
                .tenantGstin(t.getGstin())
                .tenantPan(t.getPanNumber())
                .tenantAddress(t.getAddress())
                .tenantCity(t.getCity())
                .tenantState(t.getState())
                .tenantPincode(t.getPincode())
                .tenantBankName(t.getBankName())
                .tenantAccountNumber(t.getAccountNumber())
                .tenantIfscCode(t.getIfscCode())
                .tenantBranchName(t.getBranchName())
                .tenantAccountHolderName(t.getAccountHolderName())
                .transportHsnSac(t.getTransportHsnSac() != null ? t.getTransportHsnSac() : "996791")
                .tenantInvoiceDescription(settings != null ? settings.getInvoiceDescription() : null)
                // Client print details
                .clientGstin(cl.getGstin())
                .clientAddress(cl.getAddress())
                .clientCity(cl.getCity() != null ? cl.getCity().getName() : null)
                .clientState(cl.getState() != null ? cl.getState().getName() : null)
                .clientPincode(cl.getPincode())
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
        Lr lr       = il.getLr();
        Order order = il.getOrder();
        // Determine billing weight (same logic as calculateFreightAmount)
        BigDecimal billingWeight;
        if (order.getBillingOn() == BillingOn.DELIVERED_WEIGHT) {
            billingWeight = lr.getDeliveredWeight() != null ? lr.getDeliveredWeight() : lr.getLoadedWeight();
        } else {
            billingWeight = lr.getLoadedWeight() != null ? lr.getLoadedWeight() : lr.getAllocatedWeight();
        }
        if (billingWeight == null) billingWeight = lr.getAllocatedWeight();

        return InvoiceLrResponse.builder()
                .id(il.getId())
                .lrId(lr.getId())
                .lrNumber(lr.getLrNumber())
                .lrDate(lr.getLrDate())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .vehicleRegistrationNumber(lr.getVehicleAllocation().getVehicle().getRegistrationNumber())
                .materialTypeName(order.getMaterialType() != null ? order.getMaterialType().getName() : null)
                .billingWeight(billingWeight)
                .freightRateType(order.getFreightRateType() != null ? order.getFreightRateType().name() : null)
                .freightRate(order.getFreightRate())
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
                .paymentModeLabel(p.getPaymentModeLabel())
                .referenceNumber(p.getReferenceNumber())
                .remarks(p.getRemarks())
                .createdById(p.getCreatedBy().getId())
                .createdByName(p.getCreatedBy().getName())
                .createdAt(p.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByOrder(Long orderId) {
        return invoiceLrRepository.findDistinctInvoicesByOrderId(orderId)
                .stream().map(this::mapToInvoiceResponse).toList();
    }
}