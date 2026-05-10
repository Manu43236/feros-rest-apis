package com.feros.api.service.impl;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.BillingCycle;
import com.feros.api.enums.NotificationType;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final NotificationService notificationService;

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
        BigDecimal baseAmount      = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount, request.getBillingCycle());
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

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
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

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .status(SubscriptionStatus.TRIAL)
                .startDate(tenant.getTrialStartDate() != null ? tenant.getTrialStartDate() : LocalDate.now())
                .endDate(newEnd)
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
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
                : calculateEndDate(previous.getEndDate() != null ? previous.getEndDate() : LocalDate.now(),
                        previous.getBillingCycle());

        BigDecimal baseAmount  = calculateBaseAmount(request.getAmount(), pricePerVehicle, vehicleCount, previous.getBillingCycle());
        BigDecimal gstAmount   = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(previous.getBillingCycle())
                .vehicleCount(vehicleCount)
                .pricePerVehicle(pricePerVehicle)
                .startDate(previous.getEndDate() != null ? previous.getEndDate() : LocalDate.now())
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

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
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
                .startDate(LocalDate.now())
                .endDate(tenant.getSubscriptionEndDate())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_SUSPENDED,
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
                .startDate(LocalDate.now())
                .endDate(tenant.getSubscriptionEndDate())
                .notes(notes)
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
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
                .map(i -> toInvoiceResponse(i, tenant.getCompanyName()))
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionHistoryResponse getCurrentSubscription(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .findFirst()
                .map(h -> toHistoryResponse(h, tenant, h.getPlan() != null ? h.getPlan().getName() : "-"))
                .orElseThrow(() -> new FerosException("No subscription found", HttpStatus.NOT_FOUND));
    }

    // ─── Scheduled Jobs ───────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoExpireSubscriptions() {
        LocalDate today = LocalDate.now();
        List<SubscriptionHistory> expired = historyRepository.findExpiredActive(today);
        for (SubscriptionHistory h : expired) {
            h.setStatus(SubscriptionStatus.EXPIRED);
            historyRepository.save(h);
            Tenant tenant = h.getTenant();
            tenant.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            tenantRepository.save(tenant);
            notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_EXPIRY,
                    "Subscription Expired",
                    "Your subscription has expired. Please contact FEROS support to renew.");
            log.info("Auto-expired subscription for tenant {}", tenant.getId());
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryWarnings() {
        LocalDate warningDate = LocalDate.now().plusDays(7);
        List<SubscriptionHistory> expiring = historyRepository.findActiveExpiringOn(warningDate);
        for (SubscriptionHistory h : expiring) {
            notificationService.sendToTenant(h.getTenant(), NotificationType.TRIAL_ENDING,
                    "Subscription Expiring Soon",
                    "Your subscription expires on " + h.getEndDate() + ". Please renew to avoid interruption.");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Amount = vehicleCount × pricePerVehicle × months (10 for annual, 1 for monthly).
     * If SA provides an explicit override amount, that is used directly.
     */
    private BigDecimal calculateBaseAmount(BigDecimal override, BigDecimal pricePerVehicle,
                                           int vehicleCount, BillingCycle cycle) {
        if (override != null) return override;
        if (pricePerVehicle == null || pricePerVehicle.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        int months = (cycle == BillingCycle.YEARLY) ? ANNUAL_MONTHS_PAID : 1;
        return pricePerVehicle.multiply(BigDecimal.valueOf(vehicleCount))
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Monthly → +1 month, Annual → +12 months calendar access (paying for 10).
     */
    private LocalDate calculateEndDate(LocalDate from, BillingCycle cycle) {
        if (cycle == null) return from.plusMonths(1);
        return cycle == BillingCycle.YEARLY ? from.plusMonths(12) : from.plusMonths(1);
    }

    private void createInvoice(SubscriptionHistory history, Tenant tenant, String planName,
                                BigDecimal totalAmount, BigDecimal amount, BigDecimal gstAmount,
                                String paymentRef, Integer vehicleCount, BigDecimal pricePerVehicle) {
        YearMonth ym    = YearMonth.now();
        long count      = invoiceRepository.countByYearAndMonth(ym.getYear(), ym.getMonthValue());
        String invoiceNumber = "INV-" + ym.format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + String.format("%04d", count + 1);

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
                .maxLorries(h.getVehicleCount())           // limit = what they paid for
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

    private SubscriptionInvoiceResponse toInvoiceResponse(SubscriptionInvoice i, String companyName) {
        return SubscriptionInvoiceResponse.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .tenantId(i.getTenant().getId())
                .companyName(companyName)
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
                .build();
    }
}
