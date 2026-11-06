package com.feros.api.service.impl;

import com.feros.api.dto.request.ConsumableRequestCreateRequest;
import com.feros.api.dto.request.ConsumableRequestIssueRequest;
import com.feros.api.dto.request.ConsumableRequestRejectRequest;
import com.feros.api.dto.response.ConsumableRequestResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.SparePart;
import com.feros.api.enums.ConsumableRequestStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.StockTransactionType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.ConsumableRequestService;
import com.feros.api.service.NotificationService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumableRequestServiceImpl implements ConsumableRequestService {

    private final ConsumableIssueRequestRepository requestRepository;
    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final SparePartRepository sparePartRepository;
    private final SparePartsInventoryRepository inventoryRepository;
    private final SparePartsTransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ConsumableRequestResponse createRequest(ConsumableRequestCreateRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .filter(v -> v.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        User requestedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        ConsumableIssueRequest req = ConsumableIssueRequest.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .requestedBy(requestedBy)
                .notes(request.getNotes())
                .status(ConsumableRequestStatus.PENDING)
                .isActive(true)
                .build();

        ConsumableIssueRequest saved = requestRepository.save(req);
        notificationService.sendToRoles(tenant,
                List.of(RoleName.STORE_KEEPER, RoleName.SERVICE_MANAGER, RoleName.ADMIN),
                NotificationType.PART_REQUESTED,
                "New DEF Request — " + vehicle.getRegistrationNumber(),
                vehicle.getRegistrationNumber() + " | Requested by " + requestedBy.getName());
        return toResponse(saved);
    }

    @Override
    public List<ConsumableRequestResponse> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return requestRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<ConsumableRequestResponse> getPending() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return requestRepository.findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
                        tenantId, ConsumableRequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<ConsumableRequestResponse> getMyRequests() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();
        return requestRepository.findByTenantIdAndRequestedByIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId, userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ConsumableRequestResponse issueRequest(Long id, ConsumableRequestIssueRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        ConsumableIssueRequest req = requestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("DEF request not found", HttpStatus.NOT_FOUND));

        if (req.getStatus() != ConsumableRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be issued", HttpStatus.BAD_REQUEST);
        }
        if (request == null || request.getSparePartId() == null) {
            throw new FerosException("Select the consumable item to issue", HttpStatus.BAD_REQUEST);
        }
        int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
        if (quantity <= 0) {
            throw new FerosException("Quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        User issuedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(request.getSparePartId(), tenantId)
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));

        SparePartsInventory inv = inventoryRepository.findByTenantIdAndSparePartId(tenantId, part.getId())
                .orElseThrow(() -> new FerosException("No inventory record found for " + part.getName(), HttpStatus.NOT_FOUND));

        if (inv.getQuantity() < quantity) {
            throw new FerosException("Insufficient stock. Only " + inv.getQuantity() + " " + part.getName()
                    + " available. Enter a quantity ≤ " + inv.getQuantity() + " or reject this request.",
                    HttpStatus.BAD_REQUEST);
        }

        inv.setQuantity(inv.getQuantity() - quantity);
        inv.setLastUpdated(TimeUtil.nowIst());
        inventoryRepository.save(inv);

        // Record the OUT drawdown attributed to the vehicle — use last purchase price as unit cost.
        BigDecimal unitCost = transactionRepository
                .findTopByTenantIdAndSparePartIdAndReferenceTypeOrderByCreatedAtDesc(
                        tenantId, part.getId(), StockReferenceType.PURCHASE)
                .map(SparePartsTransaction::getUnitCost)
                .orElse(BigDecimal.ZERO);
        SparePartsTransaction tx = SparePartsTransaction.builder()
                .tenant(req.getTenant())
                .sparePart(part)
                .vehicle(req.getVehicle())
                .transactionType(StockTransactionType.OUT)
                .quantity(quantity)
                .unitCost(unitCost)
                .totalCost(unitCost.multiply(BigDecimal.valueOf(quantity)))
                .referenceType(StockReferenceType.CONSUMABLE)
                .notes(request.getNotes() != null ? request.getNotes()
                        : "DEF issued to " + req.getVehicle().getRegistrationNumber())
                .createdBy(issuedBy)
                .build();
        transactionRepository.save(tx);

        if (inv.getQuantity() <= part.getMinStockLevel()) {
            notificationService.sendToRoles(req.getTenant(),
                    List.of(RoleName.STORE_KEEPER, RoleName.ADMIN),
                    NotificationType.LOW_STOCK,
                    "Low Stock Alert",
                    part.getName() + " is low on stock. Current: "
                            + inv.getQuantity() + ", Min: " + part.getMinStockLevel());
        }

        req.setSparePart(part);
        req.setQuantityIssued(quantity);
        req.setStatus(ConsumableRequestStatus.ISSUED);
        req.setApprovedBy(issuedBy);
        req.setApprovedAt(TimeUtil.nowIst());

        ConsumableIssueRequest issued = requestRepository.save(req);
        notificationService.sendToUser(issued.getTenant(), issued.getRequestedBy(),
                NotificationType.PART_APPROVED,
                "DEF Request Issued — " + issued.getVehicle().getRegistrationNumber(),
                quantity + "x " + part.getName() + " issued for " + issued.getVehicle().getRegistrationNumber() + ".");
        notificationService.sendToRoles(issued.getTenant(),
                List.of(RoleName.SERVICE_MANAGER, RoleName.ADMIN),
                NotificationType.PART_APPROVED,
                "DEF Issued — " + issued.getVehicle().getRegistrationNumber(),
                quantity + "x " + part.getName() + " issued to " + issued.getVehicle().getRegistrationNumber()
                        + " by " + issuedBy.getName());
        return toResponse(issued);
    }

    @Override
    @Transactional
    public ConsumableRequestResponse rejectRequest(Long id, ConsumableRequestRejectRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        ConsumableIssueRequest req = requestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("DEF request not found", HttpStatus.NOT_FOUND));

        if (req.getStatus() != ConsumableRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be rejected", HttpStatus.BAD_REQUEST);
        }

        User rejectedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        req.setStatus(ConsumableRequestStatus.REJECTED);
        req.setRejectionReason(request.getRejectionReason());
        req.setApprovedBy(rejectedBy);
        req.setApprovedAt(TimeUtil.nowIst());

        ConsumableIssueRequest rejected = requestRepository.save(req);
        notificationService.sendToUser(rejected.getTenant(), rejected.getRequestedBy(),
                NotificationType.PART_REJECTED,
                "DEF Request Rejected — " + rejected.getVehicle().getRegistrationNumber(),
                "Your DEF request for " + rejected.getVehicle().getRegistrationNumber() + " was rejected."
                        + (request.getRejectionReason() != null ? " Reason: " + request.getRejectionReason() : ""));
        notificationService.sendToRoles(rejected.getTenant(),
                List.of(RoleName.SERVICE_MANAGER, RoleName.ADMIN),
                NotificationType.PART_REJECTED,
                "DEF Request Rejected — " + rejected.getVehicle().getRegistrationNumber(),
                "DEF request for " + rejected.getVehicle().getRegistrationNumber() + " was rejected by " + rejectedBy.getName());
        return toResponse(rejected);
    }

    private ConsumableRequestResponse toResponse(ConsumableIssueRequest r) {
        return ConsumableRequestResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .vehicleId(r.getVehicle().getId())
                .vehicleRegistrationNumber(r.getVehicle().getRegistrationNumber())
                .sparePartId(r.getSparePart() != null ? r.getSparePart().getId() : null)
                .sparePartName(r.getSparePart() != null ? r.getSparePart().getName() : null)
                .quantityIssued(r.getQuantityIssued())
                .requestedById(r.getRequestedBy().getId())
                .requestedByName(r.getRequestedBy().getName())
                .approvedById(r.getApprovedBy() != null ? r.getApprovedBy().getId() : null)
                .approvedByName(r.getApprovedBy() != null ? r.getApprovedBy().getName() : null)
                .status(r.getStatus())
                .rejectionReason(r.getRejectionReason())
                .notes(r.getNotes())
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
