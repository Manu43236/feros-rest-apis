package com.feros.api.service.impl;

import com.feros.api.dto.request.TripExpenseApproveRequest;
import com.feros.api.dto.request.TripExpenseItemApproveRequest;
import com.feros.api.dto.request.TripExpenseItemRequest;
import com.feros.api.dto.request.TripExpenseRejectRequest;
import com.feros.api.dto.request.TripExpenseRequest;
import com.feros.api.dto.request.TripExpenseSettleRequest;
import com.feros.api.dto.response.TripExpenseItemResponse;
import com.feros.api.dto.response.TripExpenseResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.TenantSettings;
import com.feros.api.enums.LrStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.TripExpenseStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.TripExpenseService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripExpenseServiceImpl implements TripExpenseService {

    private final LrTripExpenseRepository tripExpenseRepository;
    private final LrTripExpenseItemRepository tripExpenseItemRepository;
    private final LrRepository lrRepository;
    private final TenantRepository tenantRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    private Lr getLrDelivered(Long lrId, Long tenantId) {
        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenantId)
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));
        if (lr.getLrStatus() != LrStatus.DELIVERED) {
            throw new FerosException("Trip expenses can only be submitted for delivered LRs", HttpStatus.BAD_REQUEST);
        }
        return lr;
    }

    private int calculateTripDays(Lr lr) {
        if (lr.getLoadedAt() == null || lr.getDeliveredAt() == null) return 1;
        long days = ChronoUnit.DAYS.between(lr.getLoadedAt().toLocalDate(), lr.getDeliveredAt().toLocalDate());
        return (int) Math.max(1, days + 1); // inclusive count
    }

    private BigDecimal calcDriverBatta(TenantSettings settings, int days) {
        return settings.getDriverBattaRate().multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal calcCleanerBatta(TenantSettings settings, int days, boolean hasClean) {
        if (!hasClean) return BigDecimal.ZERO;
        return settings.getCleanerBattaRate().multiply(BigDecimal.valueOf(days));
    }

    private void saveItems(LrTripExpense expense, List<TripExpenseItemRequest> items) {
        if (items == null || items.isEmpty()) return;
        Tenant tenant = expense.getTenant();
        List<LrTripExpenseItem> entities = items.stream().map(req ->
                LrTripExpenseItem.builder()
                        .tenant(tenant)
                        .tripExpense(expense)
                        .description(req.getDescription())
                        .amount(req.getAmount())
                        .receiptUrl(req.getReceiptUrl())
                        .build()
        ).collect(Collectors.toList());
        tripExpenseItemRepository.saveAll(entities);
    }

    private TripExpenseResponse toResponse(LrTripExpense expense) {
        List<LrTripExpenseItem> items = tripExpenseItemRepository.findByTripExpenseIdAndIsActiveTrue(expense.getId());

        BigDecimal totalFixed = expense.getDriverBatta()
                .add(expense.getCleanerBatta())
                .add(expense.getTripMamulu());

        BigDecimal totalOperational = items.stream()
                .map(LrTripExpenseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSubmitted = totalFixed.add(totalOperational);

        BigDecimal totalApproved = totalFixed.add(
                items.stream()
                        .map(i -> i.getApprovedAmount() != null ? i.getApprovedAmount() : i.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        BigDecimal balance = expense.getAdvanceAmount().subtract(totalApproved);

        Lr lr = expense.getLr();

        List<TripExpenseItemResponse> itemResponses = items.stream().map(i -> {
            boolean changed = i.getApprovedAmount() != null &&
                    i.getApprovedAmount().compareTo(i.getAmount()) != 0;
            return TripExpenseItemResponse.builder()
                    .id(i.getId())
                    .description(i.getDescription())
                    .amount(i.getAmount())
                    .approvedAmount(i.getApprovedAmount())
                    .receiptUrl(i.getReceiptUrl())
                    .amountChanged(changed)
                    .build();
        }).collect(Collectors.toList());

        return TripExpenseResponse.builder()
                .id(expense.getId())
                .lrId(lr.getId())
                .lrNumber(lr.getLrNumber())
                .driverName(lr.getDriver() != null ? lr.getDriver().getName() : null)
                .cleanerName(lr.getCleaner() != null ? lr.getCleaner().getName() : null)
                .advanceAmount(expense.getAdvanceAmount())
                .tripDays(expense.getTripDays())
                .driverBatta(expense.getDriverBatta())
                .cleanerBatta(expense.getCleanerBatta())
                .tripMamulu(expense.getTripMamulu())
                .totalFixedAmount(totalFixed)
                .totalOperationalAmount(totalOperational)
                .totalSubmittedAmount(totalSubmitted)
                .totalApprovedAmount(totalApproved)
                .balanceAmount(balance)
                .status(expense.getStatus())
                .submittedByName(expense.getSubmittedBy() != null ? expense.getSubmittedBy().getName() : null)
                .submittedAt(expense.getSubmittedAt())
                .approvedByName(expense.getApprovedBy() != null ? expense.getApprovedBy().getName() : null)
                .approvedAt(expense.getApprovedAt())
                .settlementAmount(expense.getSettlementAmount())
                .settlementNote(expense.getSettlementNote())
                .settledByName(expense.getSettledBy() != null ? expense.getSettledBy().getName() : null)
                .settledAt(expense.getSettledAt())
                .rejectedByName(expense.getRejectedBy() != null ? expense.getRejectedBy().getName() : null)
                .rejectedAt(expense.getRejectedAt())
                .rejectionReason(expense.getRejectionReason())
                .items(itemResponses)
                .createdAt(expense.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TripExpenseResponse createDraft(Long lrId, TripExpenseRequest request) {
        Tenant tenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        if (tripExpenseRepository.existsByLrIdAndIsActiveTrue(lrId)) {
            throw new FerosException("Trip expense sheet already exists for this LR", HttpStatus.BAD_REQUEST);
        }

        Lr lr = getLrDelivered(lrId, tenant.getId());

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                .orElseThrow(() -> new FerosException("Tenant settings not found", HttpStatus.NOT_FOUND));

        int tripDays = request.getTripDays() != null ? request.getTripDays() : calculateTripDays(lr);
        boolean hasClean = lr.getCleaner() != null;

        LrTripExpense expense = LrTripExpense.builder()
                .tenant(tenant)
                .lr(lr)
                .createdBy(currentUser)
                .advanceAmount(request.getAdvanceAmount() != null ? request.getAdvanceAmount() : BigDecimal.ZERO)
                .tripDays(tripDays)
                .driverBatta(calcDriverBatta(settings, tripDays))
                .cleanerBatta(calcCleanerBatta(settings, tripDays, hasClean))
                .tripMamulu(settings.getTripMamuluAmount())
                .build();

        tripExpenseRepository.save(expense);
        saveItems(expense, request.getItems());

        return toResponse(expense);
    }

    @Override
    @Transactional
    public TripExpenseResponse updateDraft(Long lrId, TripExpenseRequest request) {
        Tenant tenant = getCurrentTenant();

        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenant.getId())
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        LrTripExpense expense = tripExpenseRepository.findByLrIdAndIsActiveTrue(lrId)
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (expense.getStatus() != TripExpenseStatus.DRAFT && expense.getStatus() != TripExpenseStatus.REJECTED) {
            throw new FerosException("Only DRAFT or REJECTED expense sheets can be updated", HttpStatus.BAD_REQUEST);
        }

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                .orElseThrow(() -> new FerosException("Tenant settings not found", HttpStatus.NOT_FOUND));

        if (request.getAdvanceAmount() != null) {
            expense.setAdvanceAmount(request.getAdvanceAmount());
        }
        if (request.getTripDays() != null) {
            int tripDays = request.getTripDays();
            boolean hasClean = lr.getCleaner() != null;
            expense.setTripDays(tripDays);
            expense.setDriverBatta(calcDriverBatta(settings, tripDays));
            expense.setCleanerBatta(calcCleanerBatta(settings, tripDays, hasClean));
        }

        // Replace all items
        if (request.getItems() != null) {
            tripExpenseItemRepository.deleteByTripExpenseId(expense.getId());
            saveItems(expense, request.getItems());
        }

        tripExpenseRepository.save(expense);
        return toResponse(expense);
    }

    @Override
    @Transactional
    public TripExpenseResponse submit(Long lrId) {
        Tenant tenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        getLrDelivered(lrId, tenant.getId());

        LrTripExpense expense = tripExpenseRepository.findByLrIdAndIsActiveTrue(lrId)
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (expense.getStatus() != TripExpenseStatus.DRAFT && expense.getStatus() != TripExpenseStatus.REJECTED) {
            throw new FerosException("Only DRAFT or REJECTED expense sheets can be submitted", HttpStatus.BAD_REQUEST);
        }

        expense.setStatus(TripExpenseStatus.SUBMITTED);
        expense.setSubmittedBy(currentUser);
        expense.setSubmittedAt(TimeUtil.nowIst());
        // Clear rejection data on resubmit
        expense.setRejectedBy(null);
        expense.setRejectedAt(null);
        expense.setRejectionReason(null);
        tripExpenseRepository.save(expense);

        // Notify admin
        notificationService.sendToRoles(
                tenant,
                List.of(RoleName.ADMIN),
                NotificationType.TRIP_EXPENSE_SUBMITTED,
                "Trip Expense Submitted",
                "Expense sheet for LR " + expense.getLr().getLrNumber() + " has been submitted for review."
        );

        return toResponse(expense);
    }

    @Override
    public TripExpenseResponse getByLrId(Long lrId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenantId)
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        LrTripExpense expense = tripExpenseRepository.findByLrIdAndIsActiveTrue(lrId)
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        return toResponse(expense);
    }

    @Override
    public List<TripExpenseResponse> getAll(TripExpenseStatus status) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<LrTripExpense> expenses = status != null
                ? tripExpenseRepository.findByTenantIdAndStatusAndIsActiveTrue(tenantId, status)
                : tripExpenseRepository.findByTenantIdAndIsActiveTrue(tenantId);
        return expenses.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TripExpenseResponse approve(Long id, TripExpenseApproveRequest request) {
        Tenant tenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        LrTripExpense expense = tripExpenseRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenant.getId())
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (expense.getStatus() != TripExpenseStatus.SUBMITTED) {
            throw new FerosException("Only SUBMITTED expense sheets can be approved", HttpStatus.BAD_REQUEST);
        }

        List<LrTripExpenseItem> items = tripExpenseItemRepository.findByTripExpenseIdAndIsActiveTrue(id);
        Map<Long, LrTripExpenseItem> itemMap = items.stream()
                .collect(Collectors.toMap(LrTripExpenseItem::getId, i -> i));

        boolean anyChanged = false;
        StringBuilder changedDetails = new StringBuilder();

        if (request.getItems() != null) {
            for (TripExpenseItemApproveRequest approvalItem : request.getItems()) {
                LrTripExpenseItem item = itemMap.get(approvalItem.getItemId());
                if (item == null) continue;

                BigDecimal approved = approvalItem.getApprovedAmount() != null
                        ? approvalItem.getApprovedAmount()
                        : item.getAmount();

                if (approved.compareTo(item.getAmount()) != 0) {
                    anyChanged = true;
                    changedDetails.append(item.getDescription())
                            .append(": ₹").append(item.getAmount())
                            .append(" → ₹").append(approved).append(". ");
                }
                item.setApprovedAmount(approved);
                tripExpenseItemRepository.save(item);
            }
        }

        expense.setStatus(TripExpenseStatus.APPROVED);
        expense.setApprovedBy(currentUser);
        expense.setApprovedAt(TimeUtil.nowIst());
        tripExpenseRepository.save(expense);

        // Notify supervisor if any amounts were changed
        if (anyChanged && expense.getSubmittedBy() != null) {
            notificationService.sendToUser(
                    tenant,
                    expense.getSubmittedBy(),
                    NotificationType.TRIP_EXPENSE_AMOUNT_CHANGED,
                    "Trip Expense Amounts Updated",
                    "Admin modified expense amounts for LR " + expense.getLr().getLrNumber() + ". " + changedDetails
            );
        }

        // Notify supervisor of approval
        if (expense.getSubmittedBy() != null) {
            notificationService.sendToUser(
                    tenant,
                    expense.getSubmittedBy(),
                    NotificationType.TRIP_EXPENSE_APPROVED,
                    "Trip Expense Approved",
                    "Expense sheet for LR " + expense.getLr().getLrNumber() + " has been approved."
            );
        }

        return toResponse(expense);
    }

    @Override
    @Transactional
    public TripExpenseResponse settle(Long id, TripExpenseSettleRequest request) {
        Tenant tenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        LrTripExpense expense = tripExpenseRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenant.getId())
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (expense.getStatus() != TripExpenseStatus.APPROVED) {
            throw new FerosException("Only APPROVED expense sheets can be settled", HttpStatus.BAD_REQUEST);
        }

        expense.setSettlementAmount(request.getSettlementAmount());
        expense.setSettlementNote(request.getSettlementNote());
        expense.setSettledBy(currentUser);
        expense.setSettledAt(TimeUtil.nowIst());
        expense.setStatus(TripExpenseStatus.SETTLED);
        tripExpenseRepository.save(expense);

        return toResponse(expense);
    }

    @Override
    @Transactional
    public TripExpenseResponse reject(Long id, TripExpenseRejectRequest request) {
        Tenant tenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        LrTripExpense expense = tripExpenseRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenant.getId())
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (expense.getStatus() != TripExpenseStatus.SUBMITTED) {
            throw new FerosException("Only SUBMITTED expense sheets can be rejected", HttpStatus.BAD_REQUEST);
        }

        expense.setStatus(TripExpenseStatus.REJECTED);
        expense.setRejectedBy(currentUser);
        expense.setRejectedAt(TimeUtil.nowIst());
        expense.setRejectionReason(request != null ? request.getRejectionReason() : null);
        tripExpenseRepository.save(expense);

        // Notify supervisor
        if (expense.getSubmittedBy() != null) {
            notificationService.sendToUser(
                    tenant,
                    expense.getSubmittedBy(),
                    NotificationType.TRIP_EXPENSE_REJECTED,
                    "Trip Expense Rejected",
                    "Expense sheet for LR " + expense.getLr().getLrNumber() + " was rejected. " +
                    (expense.getRejectionReason() != null ? "Reason: " + expense.getRejectionReason() : "")
            );
        }

        return toResponse(expense);
    }

    @Override
    @Transactional
    public void deleteDraft(Long lrId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        LrTripExpense expense = tripExpenseRepository.findByLrIdAndIsActiveTrue(lrId)
                .orElseThrow(() -> new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND));

        if (!expense.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Trip expense sheet not found", HttpStatus.NOT_FOUND);
        }

        if (expense.getStatus() != TripExpenseStatus.DRAFT && expense.getStatus() != TripExpenseStatus.REJECTED) {
            throw new FerosException("Only DRAFT or REJECTED expense sheets can be deleted", HttpStatus.BAD_REQUEST);
        }

        expense.setIsActive(false);
        tripExpenseRepository.save(expense);
    }
}
