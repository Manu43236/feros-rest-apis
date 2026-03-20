package com.feros.api.service.impl;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.entity.*;
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

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SubscriptionHistoryResponse activate(Long tenantId, ActivateSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        SubscriptionPlan plan = planRepository.findByIdAndIsActiveTrue(request.getPlanId())
                .orElseThrow(() -> new FerosException("Plan not found or inactive", HttpStatus.NOT_FOUND));

        BigDecimal baseAmount = request.getAmount() != null ? request.getAmount()
                : (request.getBillingCycle().name().equals("YEARLY") ? plan.getPriceYearly() : plan.getPriceMonthly());
        BigDecimal gstAmount = baseAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(gstAmount);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(request.getBillingCycle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amount(baseAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .paymentRef(request.getPaymentRef())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        // Update tenant subscription fields
        tenant.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        tenant.setSubscriptionStartDate(request.getStartDate());
        tenant.setSubscriptionEndDate(request.getEndDate());
        tenantRepository.save(tenant);

        // Generate invoice
        createInvoice(history, tenant, plan.getName(), totalAmount, baseAmount, gstAmount, request.getPaymentRef());

        // Notify
        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Activated",
                "Your " + plan.getName() + " plan has been activated until " + request.getEndDate() + ".");

        return toHistoryResponse(history, tenant, plan.getName());
    }

    @Override
    @Transactional
    public SubscriptionHistoryResponse extendTrial(Long tenantId, ExtendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        if (tenant.getSubscriptionStatus() != SubscriptionStatus.TRIAL) {
            throw new FerosException("Tenant is not on trial", HttpStatus.BAD_REQUEST);
        }
        tenant.setTrialEndDate(request.getNewEndDate());
        tenantRepository.save(tenant);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .status(SubscriptionStatus.TRIAL)
                .startDate(tenant.getTrialStartDate() != null ? tenant.getTrialStartDate() : LocalDate.now())
                .endDate(request.getNewEndDate())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
                "Trial Extended",
                "Your trial has been extended to " + request.getNewEndDate() + ".");

        return toHistoryResponse(history, tenant, "Trial");
    }

    @Override
    @Transactional
    public SubscriptionHistoryResponse extendSubscription(Long tenantId, ExtendSubscriptionRequest request) {
        Tenant tenant = getTenant(tenantId);
        tenant.setSubscriptionEndDate(request.getNewEndDate());
        if (tenant.getSubscriptionStatus() == SubscriptionStatus.EXPIRED) {
            tenant.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        }
        tenantRepository.save(tenant);

        SubscriptionHistory latestHistory = historyRepository
                .findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().findFirst()
                .orElseThrow(() -> new FerosException("No subscription history found", HttpStatus.NOT_FOUND));

        SubscriptionHistory history = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(latestHistory.getPlan())
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(latestHistory.getBillingCycle())
                .startDate(latestHistory.getEndDate())
                .endDate(request.getNewEndDate())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Extended",
                "Your subscription has been extended to " + request.getNewEndDate() + ".");

        String planName = latestHistory.getPlan() != null ? latestHistory.getPlan().getName() : "-";
        return toHistoryResponse(history, tenant, planName);
    }

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
                .endDate(tenant.getSubscriptionEndDate() != null ? tenant.getSubscriptionEndDate() : LocalDate.now())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_SUSPENDED,
                "Subscription Suspended",
                "Your subscription has been suspended. Reason: " + request.getNotes());

        return toHistoryResponse(history, tenant, "-");
    }

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
                .endDate(tenant.getSubscriptionEndDate() != null ? tenant.getSubscriptionEndDate() : LocalDate.now())
                .notes(notes)
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();
        history = historyRepository.save(history);

        notificationService.sendToTenant(tenant, NotificationType.SUBSCRIPTION_ACTIVATED,
                "Subscription Reactivated",
                "Your subscription has been reactivated.");

        return toHistoryResponse(history, tenant, "-");
    }

    @Override
    public List<SubscriptionHistoryResponse> getHistory(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        return historyRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(h -> toHistoryResponse(h, tenant,
                        h.getPlan() != null ? h.getPlan().getName() : "-"))
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
                .map(h -> toHistoryResponse(h, tenant,
                        h.getPlan() != null ? h.getPlan().getName() : "-"))
                .orElseThrow(() -> new FerosException("No subscription found", HttpStatus.NOT_FOUND));
    }

    // ─── Scheduled Jobs ──────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 1 * * *") // daily at 01:00
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
                    "Your subscription has expired. Please renew to continue using FEROS.");
            log.info("Auto-expired subscription for tenant {}", tenant.getId());
        }
    }

    @Scheduled(cron = "0 0 9 * * *") // daily at 09:00 - warn 7 days before expiry
    public void sendExpiryWarnings() {
        LocalDate warningDate = LocalDate.now().plusDays(7);
        List<SubscriptionHistory> expiring = historyRepository.findActiveExpiringOn(warningDate);
        for (SubscriptionHistory h : expiring) {
            notificationService.sendToTenant(h.getTenant(), NotificationType.TRIAL_ENDING,
                    "Subscription Expiring Soon",
                    "Your subscription expires on " + h.getEndDate() + ". Please renew to avoid interruption.");
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private void createInvoice(SubscriptionHistory history, Tenant tenant, String planName,
                                BigDecimal totalAmount, BigDecimal amount, BigDecimal gstAmount,
                                String paymentRef) {
        YearMonth ym = YearMonth.now();
        long count = invoiceRepository.countByYearAndMonth(ym.getYear(), ym.getMonthValue());
        String invoiceNumber = "INV-" + ym.format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + String.format("%04d", count + 1);

        SubscriptionInvoice invoice = SubscriptionInvoice.builder()
                .invoiceNumber(invoiceNumber)
                .subscriptionHistory(history)
                .tenant(tenant)
                .planName(planName)
                .billingCycle(history.getBillingCycle() != null ? history.getBillingCycle().name() : null)
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
        return SubscriptionHistoryResponse.builder()
                .id(h.getId())
                .tenantId(tenant.getId())
                .companyName(tenant.getCompanyName())
                .planName(planName)
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
