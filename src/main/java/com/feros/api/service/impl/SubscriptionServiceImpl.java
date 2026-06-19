package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.CorrectSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.request.UpgradeRequestRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.dto.response.UpgradeRequestResponse;
import com.feros.api.enums.UpgradeRequestStatus;
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

    private static final BigDecimal GST_RATE          = new BigDecimal("0.18");
    private static final int        ANNUAL_MONTHS_PAID = 10; // pay 10, get 12 (2 months free)

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final UpgradeRequestRepository upgradeRequestRepository;
    private final NotificationService notificationService;
    private final SubscriptionInvoicePdfService subscriptionInvoicePdfService;

    // ─── Activate ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse activate(Long tenantId, ActivateSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        SubscriptionPlan plan = planRepository.findByIdAndIsActiveTrue(request.getPlanId())
                .orElseThrow(() -> new FerosException("Plan not found or inactive", HttpStatus.NOT_FOUND));

        int vehicleCount = request.getVehicleCount();
        boolean isFree   = plan.getPricePerVehicle() == null
                           || plan.getPricePerVehicle().compareTo(BigDecimal.ZERO) == 0;

        // Calculate amounts
        BigDecimal pricePerVehicle = isFree ? BigDecimal.ZERO : plan.getPricePerVehicle();
        int planMinVehicles = plan.getMinVehicles() != null ? plan.getMinVehicles() : 0;
        BigDecimal baseAmount      = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount, request.getBillingCycle(), planMinVehicles);
        BigDecimal gstAmount       = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount     = baseAmount.add(gstAmount);

        // End date: free = null (never expires), monthly = +1 month, annual = +12 months
        LocalDate endDate = isFree ? null : calculateEndDate(request.getStartDate(), request.getBillingCycle());

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(plan)
                .status(isFree ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE)
                .billingCycle(isFree ? null : request.getBillingCycle())
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(request.getStartDate())
                .endDate(endDate)
                .amount(baseAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(request.getPaymentRef())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        // Update tenant
        tenant.setSubscriptionStatus(isFree ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE);
        tenant.setSubscriptionStartDate(request.getStartDate());
        tenant.setSubscriptionEndDate(endDate);
        tenantRepository.save(tenant);

        // Invoice (skip for free plan)
        if (!isFree) {
            createInvoice(history, tenant, plan.getName(), totalAmount, baseAmount, gstAmount,
                    request.getPaymentRef(), vehicleCount, pricePerVehicle);
        }

        // Mark any PENDING upgrade request for this tenant as FULFILLED
        upgradeRequestRepository.findFirstByTenant_IdAndStatusOrderByCreatedAtDesc(
                tenantId, UpgradeRequestStatus.PENDING)
                .ifPresent(ur -> {
                    ur.setStatus(UpgradeRequestStatus.FULFILLED);
                    upgradeRequestRepository.save(ur);
                });

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Activated",
                "Your " + plan.getName() + " plan has been activated"
                + (endDate != null ? " until " + endDate : "") + ".");

        return toHistoryResponse(history, tenant, plan.getName());
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

        SubscriptionPlan trialPlan = planRepository.findByNameIgnoreCase("Trial").orElse(null);
        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(trialPlan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(tenant.getTrialStartDate() != null ? tenant.getTrialStartDate() : TimeUtil.today())
                .endDate(newEnd)
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Trial Extended", "Your trial has been extended to " + newEnd + ".");

        return toHistoryResponse(history, tenant, "Trial");
    }

    // ─── Extend / Renew ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse extendSubscription(Long tenantId, ExtendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);

        // Close the current ACTIVE row as RENEWED
        SubscriptionHistory previous = historyRepository
                .findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .filter(h -> h.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new FerosException("No active subscription found", HttpStatus.NOT_FOUND));

        previous.setStatus(SubscriptionStatus.RENEWED);
        historyRepository.save(previous);

        SubscriptionPlan plan = previous.getPlan();

        // Vehicle count: use new count from request or keep previous
        int vehicleCount = request.getVehicleCount() != null
                ? request.getVehicleCount()
                : (previous.getVehicleCount() != null ? previous.getVehicleCount() : 1);

        BigDecimal pricePerVehicle = previous.getPricePerVehicle() != null
                ? previous.getPricePerVehicle()
                : (plan != null && plan.getPricePerVehicle() != null ? plan.getPricePerVehicle() : BigDecimal.ZERO);

        // New end date: provided or auto-calculated from billing cycle
        LocalDate newEnd = request.getNewEndDate() != null
                ? request.getNewEndDate()
                : calculateEndDate(previous.getEndDate() != null ? previous.getEndDate() : TimeUtil.today(),
                        previous.getBillingCycle());

        int extendMinVehicles = plan != null && plan.getMinVehicles() != null ? plan.getMinVehicles() : 0;
        BigDecimal baseAmount  = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount, previous.getBillingCycle(), extendMinVehicles);
        BigDecimal gstAmount   = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(previous.getBillingCycle())
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
            String planName = plan != null ? plan.getName() : "-";
            createInvoice(history, tenant, planName, totalAmount, baseAmount, gstAmount,
                    request.getPaymentRef(), vehicleCount, pricePerVehicle);
        }

        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Renewed", "Your subscription has been renewed until " + newEnd + ".");

        String planName = plan != null ? plan.getName() : "-";
        return toHistoryResponse(history, tenant, planName);
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

        return toHistoryResponse(history, tenant, "-");
    }

    // ─── Reactivate ───────────────────────────────────────────────────────────

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

        return toHistoryResponse(history, tenant, "-");
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<SubscriptionHistoryResponse> getHistory(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(h -> toHistoryResponse(h, tenant, h.getPlan() != null ? h.getPlan().getName() : "-"))
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
                .orElseThrow(() -> new com.feros.api.exception.FerosException("Invoice not found", org.springframework.http.HttpStatus.NOT_FOUND));
        return toInvoiceResponse(invoice, tenant);
    }

    @Override
    public byte[] generateInvoicePdf(Long tenantId, Long invoiceId) {
        SubscriptionInvoice invoice = invoiceRepository.findByIdAndTenant_Id(invoiceId, tenantId)
                .orElseThrow(() -> new com.feros.api.exception.FerosException("Invoice not found", org.springframework.http.HttpStatus.NOT_FOUND));
        return subscriptionInvoicePdfService.generate(invoice);
    }

    @Override
    public SubscriptionHistoryResponse getCurrentSubscription(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .findFirst()
                .map(h -> toHistoryResponse(h, tenant, h.getPlan() != null ? h.getPlan().getName() : "-"))
                .orElse(null);
    }

    // ─── Scheduled Jobs ───────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoExpireSubscriptions() {
        LocalDate today = TimeUtil.today();

        // Expire ACTIVE subscriptions
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

        // Expire TRIAL subscriptions
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

        // 7-day warning — ACTIVE
        historyRepository.findActiveExpiringOn(sevenDays).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Subscription Expiring in 7 Days",
                        "Your subscription expires on " + h.getEndDate() + ". Please renew to avoid interruption."));

        // 7-day warning — TRIAL
        historyRepository.findTrialsExpiringOn(sevenDays).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Trial Ending in 7 Days",
                        "Your free trial expires on " + h.getEndDate() + ". Contact FEROS to activate a paid plan."));

        // 1-day warning — ACTIVE
        historyRepository.findActiveExpiringOn(tomorrow).forEach(h ->
                notificationService.sendToRoles(h.getTenant(), List.of(RoleName.ADMIN), NotificationType.TRIAL_ENDING,
                        "Subscription Expires Tomorrow",
                        "Your subscription expires tomorrow (" + h.getEndDate() + "). Renew now to avoid access loss."));

        // 1-day warning — TRIAL
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

    /**
     * Amount = vehicleCount × pricePerVehicle × months.
     * 2-months-free (pay 10, get 12) applies only to plans with minVehicles >= 250 (Enterprise+).
     * All other plans pay full 12 months for annual billing.
     * If SA provides an explicit override amount, that is used directly.
     */
    private BigDecimal calculateBaseAmount(BigDecimal override, BigDecimal pricePerVehicle,
                                           int vehicleCount, BillingCycle cycle, int minVehicles) {
        if (override != null) return override;
        if (pricePerVehicle == null || pricePerVehicle.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        int months;
        if (cycle == BillingCycle.YEARLY) {
            months = minVehicles >= 250 ? ANNUAL_MONTHS_PAID : 12;
        } else if (cycle == BillingCycle.SIX_MONTHS) {
            months = 6;
        } else if (cycle == BillingCycle.THREE_MONTHS) {
            months = 3;
        } else {
            months = 1;
        }
        return pricePerVehicle.multiply(BigDecimal.valueOf(vehicleCount))
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Monthly → +1 month, Annual → +12 months calendar access (paying for 10).
     */
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
                                String paymentRef, Integer vehicleCount, BigDecimal pricePerVehicle) {
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
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(paymentRef)
                .build();
        invoiceRepository.save(invoice);
    }

    private SubscriptionHistoryResponse toHistoryResponse(SubscriptionHistory h, Tenant tenant, String planName) {
        SubscriptionPlan plan = h.getPlan();
        return SubscriptionHistoryResponse.builder()
                .id(h.getId())
                .tenantId(tenant.getId())
                .companyName(tenant.getCompanyName())
                .planName(planName)
                .vehicleCount(h.getVehicleCount())
                .pricePerVehicle(h.getPricePerVehicle())
                .maxLorries(h.getVehicleCount() != null ? h.getVehicleCount()
                        : (plan != null && plan.getMaxVehicles() != null && plan.getMaxVehicles() > 0
                                ? plan.getMaxVehicles() : -1))  // paid = vehicleCount, trial = plan max, unlimited = -1
                .maxUsers(plan != null ? plan.getMaxUsers() : null)
                // Feature flags — all true if no plan (backward compat)
                .hasFuelLogs(plan == null || Boolean.TRUE.equals(plan.getHasFuelLogs()))
                .hasMeterReadings(plan == null || Boolean.TRUE.equals(plan.getHasMeterReadings()))
                .hasVehicleServices(plan == null || Boolean.TRUE.equals(plan.getHasVehicleServices()))
                .hasAttendance(plan == null || Boolean.TRUE.equals(plan.getHasAttendance()))
                .hasPayroll(plan == null || Boolean.TRUE.equals(plan.getHasPayroll()))
                .hasInventory(plan == null || Boolean.TRUE.equals(plan.getHasInventory()))
                .hasReports(plan == null || Boolean.TRUE.equals(plan.getHasReports()))
                .hasCreditNotes(plan == null || Boolean.TRUE.equals(plan.getHasCreditNotes()))
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

    // ─── Correct ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionHistoryResponse correctSubscription(Long tenantId, CorrectSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);

        SubscriptionHistory current = historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .filter(h -> h.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new FerosException("No active subscription to correct", HttpStatus.NOT_FOUND));

        int vehicleCount = request.getVehicleCount() != null
                ? request.getVehicleCount()
                : (current.getVehicleCount() != null ? current.getVehicleCount() : 1);

        BigDecimal pricePerVehicle = current.getPricePerVehicle() != null
                ? current.getPricePerVehicle() : BigDecimal.ZERO;

        BigDecimal baseAmount;
        if (request.getAmount() != null) {
            baseAmount = request.getAmount();
        } else if (request.getVehicleCount() != null
                && !request.getVehicleCount().equals(current.getVehicleCount())) {
            int planMin = current.getPlan() != null && current.getPlan().getMinVehicles() != null
                    ? current.getPlan().getMinVehicles() : 0;
            baseAmount = calculateBaseAmount(null, pricePerVehicle, vehicleCount, current.getBillingCycle(), planMin);
        } else {
            baseAmount = current.getAmount() != null ? current.getAmount() : BigDecimal.ZERO;
        }
        BigDecimal gstAmount   = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        String paymentRef = request.getPaymentRef() != null ? request.getPaymentRef() : current.getPaymentRef();

        SubscriptionHistory correction = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(current.getPlan())
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(current.getBillingCycle())
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(current.getStartDate())
                .endDate(current.getEndDate())
                .amount(baseAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(paymentRef)
                .notes("[CORRECTION] " + request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        correction = historyRepository.save(correction);

        String planName = current.getPlan() != null ? current.getPlan().getName() : "-";
        return toHistoryResponse(correction, tenant, planName);
    }

    // ─── Upgrade Requests ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public UpgradeRequestResponse submitUpgradeRequest(Long tenantId, UpgradeRequestRequest request) {
        Tenant tenant = getTenant(tenantId);
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new FerosException("Plan not found", HttpStatus.NOT_FOUND));

        int months = (request.getBillingCycle() == BillingCycle.YEARLY)
                ? ((plan.getMinVehicles() != null && plan.getMinVehicles() >= 250) ? ANNUAL_MONTHS_PAID : 12)
                : 1;
        BigDecimal base  = plan.getPricePerVehicle()
                .multiply(BigDecimal.valueOf(request.getVehicleCount()))
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(base.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP));

        com.feros.api.entity.UpgradeRequest ur = com.feros.api.entity.UpgradeRequest.builder()
                .tenant(tenant)
                .plan(plan)
                .vehicleCount(request.getVehicleCount())
                .billingCycle(request.getBillingCycle())
                .notes(request.getNotes())
                .status(UpgradeRequestStatus.PENDING)
                .createdAt(TimeUtil.nowIst())
                .build();
        ur = upgradeRequestRepository.save(ur);

        // Confirm to tenant
        notificationService.sendToRoles(tenant, List.of(RoleName.ADMIN), NotificationType.UPGRADE_REQUEST,
                "Upgrade Request Received",
                "Your request to upgrade to " + plan.getName() + " plan (" + request.getVehicleCount()
                        + " vehicles) has been received. We'll contact you shortly.");

        return toUpgradeRequestResponse(ur, plan, tenant.getCompanyName(), base, total);
    }

    @Override
    public List<UpgradeRequestResponse> getUpgradeRequests() {
        return upgradeRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ur -> {
                    BigDecimal base = BigDecimal.ZERO, total = BigDecimal.ZERO;
                    if (ur.getPlan() != null && ur.getPlan().getPricePerVehicle() != null && ur.getVehicleCount() != null) {
                        int months = (ur.getBillingCycle() == BillingCycle.YEARLY)
                                ? ((ur.getPlan().getMinVehicles() != null && ur.getPlan().getMinVehicles() >= 250) ? ANNUAL_MONTHS_PAID : 12)
                                : 1;
                        base  = ur.getPlan().getPricePerVehicle()
                                .multiply(BigDecimal.valueOf(ur.getVehicleCount()))
                                .multiply(BigDecimal.valueOf(months))
                                .setScale(2, RoundingMode.HALF_UP);
                        total = base.add(base.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP));
                    }
                    return toUpgradeRequestResponse(ur,
                            ur.getPlan(),
                            ur.getTenant().getCompanyName(), base, total);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void dismissUpgradeRequest(Long id) {
        com.feros.api.entity.UpgradeRequest ur = upgradeRequestRepository.findById(id)
                .orElseThrow(() -> new FerosException("Upgrade request not found", HttpStatus.NOT_FOUND));
        ur.setStatus(UpgradeRequestStatus.DISMISSED);
        upgradeRequestRepository.save(ur);
    }

    private UpgradeRequestResponse toUpgradeRequestResponse(
            com.feros.api.entity.UpgradeRequest ur, SubscriptionPlan plan,
            String companyName, BigDecimal base, BigDecimal total) {
        return UpgradeRequestResponse.builder()
                .id(ur.getId())
                .tenantId(ur.getTenant().getId())
                .companyName(companyName)
                .planId(plan != null ? plan.getId() : null)
                .planName(plan != null ? plan.getName() : null)
                .pricePerVehicle(plan != null ? plan.getPricePerVehicle() : null)
                .vehicleCount(ur.getVehicleCount())
                .billingCycle(ur.getBillingCycle())
                .estimatedBase(base)
                .estimatedTotal(total)
                .notes(ur.getNotes())
                .status(ur.getStatus())
                .createdAt(ur.getCreatedAt())
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
