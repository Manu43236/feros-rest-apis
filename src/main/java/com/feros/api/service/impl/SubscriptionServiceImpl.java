package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.CorrectSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.BillingCycle;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.SubscriptionStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.SubscriptionService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final TenantRepository tenantRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final NotificationService notificationService;
    private final SubscriptionInvoicePdfService subscriptionInvoicePdfService;

    // ─── Activate ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse activate(Long tenantId, ActivateSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);

        int vehicleCount = request.getVehicleCount();
        BigDecimal pricePerVehicle = request.getPricePerVehicle() != null
                ? request.getPricePerVehicle()
                : BigDecimal.ZERO;

        BigDecimal baseAmount          = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount,
                request.getBillingCycle());
        BigDecimal installationCharges = request.getInstallationCharges() != null
                ? request.getInstallationCharges() : BigDecimal.ZERO;
        BigDecimal taxableAmount = baseAmount.add(installationCharges);
        BigDecimal gstAmount     = taxableAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount   = taxableAmount.add(gstAmount);

        LocalDate endDate = calculateEndDate(request.getStartDate(), request.getBillingCycle());
        String planName   = request.getPlanName() != null ? request.getPlanName() : "Custom";

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .planName(planName)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(request.getBillingCycle())
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(request.getStartDate())
                .endDate(endDate)
                .amount(baseAmount)
                .installationCharges(installationCharges.compareTo(BigDecimal.ZERO) > 0 ? installationCharges : null)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(request.getPaymentRef())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        tenant.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        tenant.setSubscriptionStartDate(request.getStartDate());
        tenant.setSubscriptionEndDate(endDate);
        tenantRepository.save(tenant);

        if (taxableAmount.compareTo(BigDecimal.ZERO) > 0) {
            createInvoice(history, tenant, planName, totalAmount, baseAmount, gstAmount,
                    request.getPaymentRef(), vehicleCount, pricePerVehicle, installationCharges);
        }

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Activated",
                "Your " + planName + " plan has been activated until " + endDate + ".");

        return toHistoryResponse(history, tenant);
    }

    // ─── Extend Trial ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse extendTrial(Long tenantId, ExtendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        if (tenant.getSubscriptionStatus() != SubscriptionStatus.TRIAL) {
            throw new FerosException("Tenant is not on trial", HttpStatus.BAD_REQUEST);
        }
        LocalDate newEnd = request.getNewEndDate();
        if (newEnd == null) throw new FerosException("New end date is required to extend trial", HttpStatus.BAD_REQUEST);

        tenant.setTrialEndDate(newEnd);
        tenantRepository.save(tenant);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .planName("Trial")
                .status(SubscriptionStatus.TRIAL)
                .startDate(tenant.getTrialStartDate() != null ? tenant.getTrialStartDate() : TimeUtil.today())
                .endDate(newEnd)
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Trial Extended", "Your trial has been extended to " + newEnd + ".");

        return toHistoryResponse(history, tenant);
    }

    // ─── Extend / Renew ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse extendSubscription(Long tenantId, ExtendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);

        // Find the most recent ACTIVE or EXPIRED history row to renew from
        SubscriptionHistory previous = historyRepository
                .findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .filter(h -> h.getStatus() == SubscriptionStatus.ACTIVE
                          || h.getStatus() == SubscriptionStatus.EXPIRED)
                .findFirst()
                .orElseThrow(() -> new FerosException("No active or expired subscription found to renew", HttpStatus.NOT_FOUND));

        previous.setStatus(SubscriptionStatus.RENEWED);
        historyRepository.save(previous);

        // Vehicle count: new or keep previous
        int vehicleCount = request.getVehicleCount() != null
                ? request.getVehicleCount()
                : (previous.getVehicleCount() != null ? previous.getVehicleCount() : 1);

        // Price per vehicle: new override > previous rate > 0
        BigDecimal pricePerVehicle = request.getPricePerVehicle() != null
                ? request.getPricePerVehicle()
                : (previous.getPricePerVehicle() != null ? previous.getPricePerVehicle() : BigDecimal.ZERO);

        // Plan name: new override > previous
        String planName = request.getPlanName() != null
                ? request.getPlanName()
                : (previous.getPlanName() != null ? previous.getPlanName() : "Custom");

        // Billing cycle from previous
        BillingCycle billingCycle = previous.getBillingCycle();

        // End date: provided or auto-calculated
        LocalDate newEnd = request.getNewEndDate() != null
                ? request.getNewEndDate()
                : calculateEndDate(previous.getEndDate() != null ? previous.getEndDate() : TimeUtil.today(), billingCycle);

        BigDecimal baseAmount  = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount, billingCycle);
        BigDecimal gstAmount   = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .planName(planName)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(billingCycle)
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(previous.getEndDate() != null ? previous.getEndDate() : TimeUtil.today())
                .endDate(newEnd)
                .amount(baseAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(request.getPaymentRef())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        tenant.setSubscriptionEndDate(newEnd);
        tenant.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);

        if (baseAmount.compareTo(BigDecimal.ZERO) > 0) {
            createInvoice(history, tenant, planName, totalAmount, baseAmount, gstAmount,
                    request.getPaymentRef(), vehicleCount, pricePerVehicle, BigDecimal.ZERO);
        }

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Renewed", "Your subscription has been renewed until " + newEnd + ".");

        return toHistoryResponse(history, tenant);
    }

    // ─── Suspend ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse suspend(Long tenantId, SuspendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        tenant.setSubscriptionStatus(SubscriptionStatus.SUSPENDED);
        tenantRepository.save(tenant);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .status(SubscriptionStatus.SUSPENDED)
                .startDate(TimeUtil.today())
                .endDate(tenant.getSubscriptionEndDate())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_SUSPENDED,
                "Subscription Suspended",
                "Your subscription has been suspended. Reason: " + request.getNotes());

        return toHistoryResponse(history, tenant);
    }

    // ─── Reactivate (SUSPENDED → ACTIVE only) ────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse reactivate(Long tenantId, String notes) {
        Tenant tenant = getTenant(tenantId);
        if (tenant.getSubscriptionStatus() != SubscriptionStatus.SUSPENDED) {
            throw new FerosException("Tenant subscription is not suspended", HttpStatus.BAD_REQUEST);
        }
        tenant.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(TimeUtil.today())
                .endDate(tenant.getSubscriptionEndDate())
                .notes(notes)
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Reactivated", "Your subscription has been reactivated.");

        return toHistoryResponse(history, tenant);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<SubscriptionHistoryResponse> getHistory(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(h -> toHistoryResponse(h, tenant))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionInvoiceResponse> getInvoices(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return invoiceRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(i -> toInvoiceResponse(i, tenant))
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionInvoiceResponse getInvoiceById(Long tenantId, Long invoiceId) {
        Tenant tenant = getTenant(tenantId);
        SubscriptionInvoice invoice = invoiceRepository.findByIdAndTenant_Id(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        return toInvoiceResponse(invoice, tenant);
    }

    @Override
    public byte[] generateInvoicePdf(Long tenantId, Long invoiceId) {
        SubscriptionInvoice invoice = invoiceRepository.findByIdAndTenant_Id(invoiceId, tenantId)
                .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
        return subscriptionInvoicePdfService.generate(invoice);
    }

    @Override
    public SubscriptionHistoryResponse getCurrentSubscription(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .findFirst()
                .map(h -> toHistoryResponse(h, tenant))
                .orElse(null);
    }

    // ─── Correct ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse correctSubscription(Long tenantId, CorrectSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);

        SubscriptionHistory current = historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new FerosException("No subscription history to correct", HttpStatus.NOT_FOUND));

        int vehicleCount = request.getVehicleCount() != null
                ? request.getVehicleCount()
                : (current.getVehicleCount() != null ? current.getVehicleCount() : 1);

        BigDecimal pricePerVehicle = request.getPricePerVehicle() != null
                ? request.getPricePerVehicle()
                : (current.getPricePerVehicle() != null ? current.getPricePerVehicle() : BigDecimal.ZERO);

        String planName = request.getPlanName() != null
                ? request.getPlanName()
                : (current.getPlanName() != null ? current.getPlanName() : "Custom");

        BillingCycle billingCycle = current.getBillingCycle();
        if (request.getBillingCycle() != null) {
            try { billingCycle = BillingCycle.valueOf(request.getBillingCycle()); } catch (IllegalArgumentException ignored) {}
        }

        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : current.getEndDate();

        BigDecimal baseAmount;
        if (request.getAmount() != null) {
            baseAmount = request.getAmount();
        } else {
            baseAmount = calculateBaseAmount(null, pricePerVehicle, vehicleCount, billingCycle);
        }
        BigDecimal gstAmount   = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        String paymentRef = request.getPaymentRef() != null ? request.getPaymentRef() : current.getPaymentRef();

        SubscriptionHistory correction = SubscriptionHistory.builder()
                .tenant(tenant)
                .planName(planName)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(billingCycle)
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(current.getStartDate())
                .endDate(endDate)
                .amount(baseAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(paymentRef)
                .notes("[CORRECTION] " + request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        correction = historyRepository.save(correction);

        if (request.getEndDate() != null) {
            tenant.setSubscriptionEndDate(endDate);
            tenantRepository.save(tenant);
        }

        return toHistoryResponse(correction, tenant);
    }

    // ─── Scheduled Jobs ───────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoExpireSubscriptions() {
        LocalDate today = TimeUtil.today();

        for (SubscriptionHistory h : historyRepository.findExpiredActive(today)) {
            h.setStatus(SubscriptionStatus.EXPIRED);
            historyRepository.save(h);
            Tenant tenant = h.getTenant();
            tenant.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            tenantRepository.save(tenant);
            notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_EXPIRY,
                    "Subscription Expired",
                    "Your subscription has expired. Please contact FEROS support to renew.");
            log.info("Auto-expired subscription for tenant {}", tenant.getId());
        }

        for (SubscriptionHistory h : historyRepository.findExpiredTrials(today)) {
            h.setStatus(SubscriptionStatus.EXPIRED);
            historyRepository.save(h);
            Tenant tenant = h.getTenant();
            tenant.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            tenantRepository.save(tenant);
            notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_EXPIRY,
                    "Trial Expired",
                    "Your 30-day free trial has expired. Contact FEROS to activate a plan and continue using the platform.");
            log.info("Auto-expired trial for tenant {}", tenant.getId());
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryWarnings() {
        LocalDate sevenDays = TimeUtil.today().plusDays(7);
        LocalDate tomorrow  = TimeUtil.today().plusDays(1);

        historyRepository.findActiveExpiringOn(sevenDays).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Subscription Expiring in 7 Days",
                        "Your subscription expires on " + h.getEndDate() + ". Please renew to avoid interruption."));

        historyRepository.findTrialsExpiringOn(sevenDays).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Trial Ending in 7 Days",
                        "Your free trial expires on " + h.getEndDate() + ". Contact FEROS to activate a paid plan."));

        historyRepository.findActiveExpiringOn(tomorrow).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Subscription Expires Tomorrow",
                        "Your subscription expires tomorrow (" + h.getEndDate() + "). Renew now to avoid access loss."));

        historyRepository.findTrialsExpiringOn(tomorrow).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Trial Expires Tomorrow",
                        "Your free trial expires tomorrow (" + h.getEndDate() + "). Contact FEROS to activate a plan."));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private BigDecimal calculateBaseAmount(BigDecimal override, BigDecimal pricePerVehicle,
                                           int vehicleCount, BillingCycle cycle) {
        if (override != null) return override;
        if (pricePerVehicle == null || pricePerVehicle.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        int months = switch (cycle != null ? cycle : BillingCycle.MONTHLY) {
            case YEARLY       -> 12;
            case SIX_MONTHS   -> 6;
            case THREE_MONTHS -> 3;
            default           -> 1;
        };
        return pricePerVehicle.multiply(BigDecimal.valueOf(vehicleCount))
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate calculateEndDate(LocalDate from, BillingCycle cycle) {
        if (cycle == null) return from.plusMonths(1);
        return switch (cycle) {
            case YEARLY       -> from.plusMonths(12);
            case SIX_MONTHS   -> from.plusMonths(6);
            case THREE_MONTHS -> from.plusMonths(3);
            default           -> from.plusMonths(1);
        };
    }

    private void createInvoice(SubscriptionHistory history, Tenant tenant, String planName,
                                BigDecimal totalAmount, BigDecimal amount, BigDecimal gstAmount,
                                String paymentRef, Integer vehicleCount, BigDecimal pricePerVehicle,
                                BigDecimal installationCharges) {
        String invoiceNumber = "INV_FEROS_SUB_"
                + TimeUtil.nowIst()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        SubscriptionInvoice invoice = SubscriptionInvoice.builder()
                .invoiceNumber(invoiceNumber)
                .subscriptionHistory(history)
                .tenant(tenant)
                .planName(planName)
                .billingCycle(history.getBillingCycle() != null ? history.getBillingCycle().name() : null)
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .periodStart(history.getStartDate())
                .periodEnd(history.getEndDate())
                .amount(amount)
                .installationCharges(installationCharges != null && installationCharges.compareTo(BigDecimal.ZERO) > 0
                        ? installationCharges : null)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(paymentRef)
                .build();
        invoiceRepository.save(invoice);
    }

    private SubscriptionHistoryResponse toHistoryResponse(SubscriptionHistory h, Tenant tenant) {
        String planName = h.getPlanName() != null ? h.getPlanName() : "-";

        return SubscriptionHistoryResponse.builder()
                .id(h.getId())
                .tenantId(tenant.getId())
                .companyName(tenant.getCompanyName())
                .planName(planName)
                .vehicleCount(h.getVehicleCount())
                .pricePerVehicle(h.getPricePerVehicle())
                .maxLorries(h.getVehicleCount() != null ? h.getVehicleCount() : -1)
                .maxUsers(-1)  // unlimited — no plan-based user limits
                // All features always included
                .hasFuelLogs(true)
                .hasMeterReadings(true)
                .hasVehicleServices(true)
                .hasAttendance(true)
                .hasPayroll(true)
                .hasInventory(true)
                .hasReports(true)
                .hasCreditNotes(true)
                .status(h.getStatus())
                .billingCycle(h.getBillingCycle())
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .amount(h.getAmount())
                .gstAmount(h.getGstAmount())
                .totalAmount(h.getTotalAmount())
                .paymentRef(h.getPaymentRef())
                .notes(h.getNotes())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private SubscriptionInvoiceResponse toInvoiceResponse(SubscriptionInvoice i, Tenant tenant) {
        return SubscriptionInvoiceResponse.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .tenantId(i.getTenant().getId())
                .companyName(tenant.getCompanyName())
                .planName(i.getPlanName())
                .billingCycle(i.getBillingCycle())
                .vehicleCount(i.getVehicleCount())
                .pricePerVehicle(i.getPricePerVehicle())
                .periodStart(i.getPeriodStart())
                .periodEnd(i.getPeriodEnd())
                .amount(i.getAmount())
                .installationCharges(i.getInstallationCharges())
                .gstAmount(i.getGstAmount())
                .totalAmount(i.getTotalAmount())
                .paymentRef(i.getPaymentRef())
                .createdAt(i.getCreatedAt())
                .tenantAddress(tenant.getAddress())
                .tenantCity(tenant.getCity())
                .tenantState(tenant.getState())
                .tenantPincode(tenant.getPincode())
                .tenantGstin(tenant.getGstin())
                .build();
    }
}
