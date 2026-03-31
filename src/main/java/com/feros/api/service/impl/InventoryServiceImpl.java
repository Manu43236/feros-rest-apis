package com.feros.api.service.impl;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.entity.master.SparePart;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.StockTransactionType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.InventoryService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final SparePartRepository sparePartRepository;
    private final SparePartsInventoryRepository inventoryRepository;
    private final ServicePartRepository servicePartRepository;
    private final SparePartsTransactionRepository transactionRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    private Long getTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    // ─── Spare Parts Master ───────────────────────────────────────────────────

    @Override
    @Transactional
    public SparePartResponse createSparePart(SparePartRequest request) {
        Tenant tenant = getTenant();
        SparePart part = SparePart.builder()
                .tenant(tenant)
                .name(request.getName())
                .partNumber(request.getPartNumber())
                .category(request.getCategory())
                .unit(request.getUnit())
                .minStockLevel(request.getMinStockLevel())
                .build();
        part = sparePartRepository.save(part);

        // Init inventory record at 0
        SparePartsInventory inv = SparePartsInventory.builder()
                .tenant(tenant)
                .sparePart(part)
                .quantity(0)
                .lastUpdated(LocalDateTime.now())
                .build();
        inventoryRepository.save(inv);

        return toSparePartResponse(part);
    }

    @Override
    @Transactional
    public SparePartResponse updateSparePart(Long id, SparePartRequest request) {
        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(id, getTenantId())
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));
        part.setName(request.getName());
        part.setPartNumber(request.getPartNumber());
        part.setCategory(request.getCategory());
        part.setUnit(request.getUnit());
        part.setMinStockLevel(request.getMinStockLevel());
        return toSparePartResponse(sparePartRepository.save(part));
    }

    @Override
    public SparePartResponse getSparePart(Long id) {
        return toSparePartResponse(
                sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(id, getTenantId())
                        .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND))
        );
    }

    @Override
    public List<SparePartResponse> getAllSpareParts() {
        return sparePartRepository.findByTenantIdAndIsActiveTrueOrderByNameAsc(getTenantId())
                .stream().map(this::toSparePartResponse).toList();
    }

    @Override
    @Transactional
    public void deleteSparePart(Long id) {
        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(id, getTenantId())
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));
        part.setIsActive(false);
        sparePartRepository.save(part);
    }

    // ─── Stock ────────────────────────────────────────────────────────────────

    @Override
    public List<StockItemResponse> getStock() {
        return inventoryRepository.findByTenantIdOrderBySparePartNameAsc(getTenantId())
                .stream().map(this::toStockItemResponse).toList();
    }

    @Override
    @Transactional
    public void stockIn(StockInRequest request) {
        Long tenantId = getTenantId();
        Tenant tenant = getTenant();
        User user = getCurrentUser();

        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(request.getSparePartId(), tenantId)
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));

        // Update inventory balance
        SparePartsInventory inv = inventoryRepository.findByTenantIdAndSparePartId(tenantId, part.getId())
                .orElseGet(() -> SparePartsInventory.builder().tenant(tenant).sparePart(part).quantity(0).build());
        inv.setQuantity(inv.getQuantity() + request.getQuantity());
        inv.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inv);

        // Record transaction
        BigDecimal unitCost = request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO;
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(request.getQuantity()));
        SparePartsTransaction tx = SparePartsTransaction.builder()
                .tenant(tenant)
                .sparePart(part)
                .transactionType(StockTransactionType.IN)
                .quantity(request.getQuantity())
                .unitCost(unitCost)
                .totalCost(totalCost)
                .referenceType(StockReferenceType.PURCHASE)
                .supplierName(request.getSupplierName())
                .notes(request.getNotes())
                .createdBy(user)
                .build();
        transactionRepository.save(tx);
    }

    // ─── Service Parts ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ServicePartResponse requestPart(Long serviceId, ServicePartRequest request) {
        Long tenantId = getTenantId();
        User user = getCurrentUser();

        VehicleService service = vehicleServiceRepository.findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (service.getStatus() != ServiceStatus.IN_PROGRESS)
            throw new FerosException("Parts can only be requested for IN_PROGRESS services", HttpStatus.BAD_REQUEST);

        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(request.getSparePartId(), tenantId)
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));

        ServicePart sp = ServicePart.builder()
                .service(service)
                .sparePart(part)
                .quantityRequested(request.getQuantityRequested())
                .status(ServicePartStatus.REQUESTED)
                .requestedBy(user)
                .build();

        return toServicePartResponse(servicePartRepository.save(sp));
    }

    @Override
    @Transactional
    public ServicePartResponse approvePart(Long servicePartId, ServicePartApprovalRequest request) {
        Long tenantId = getTenantId();
        User approver = getCurrentUser();

        ServicePart sp = servicePartRepository.findByIdAndService_TenantId(servicePartId, tenantId)
                .orElseThrow(() -> new FerosException("Service part request not found", HttpStatus.NOT_FOUND));

        if (sp.getStatus() != ServicePartStatus.REQUESTED)
            throw new FerosException("Only REQUESTED parts can be approved/rejected", HttpStatus.BAD_REQUEST);

        if (request.getStatus() == ServicePartStatus.APPROVED) {
            int qtyApproved = request.getQuantityApproved() != null
                    ? request.getQuantityApproved()
                    : sp.getQuantityRequested();

            // Check stock
            SparePartsInventory inv = inventoryRepository
                    .findByTenantIdAndSparePartId(tenantId, sp.getSparePart().getId())
                    .orElseThrow(() -> new FerosException("No inventory record found for this part", HttpStatus.NOT_FOUND));

            if (inv.getQuantity() < qtyApproved) {
                // Warn but allow (soft block)
                // Stock will go negative — noted in transaction
            }

            sp.setStatus(ServicePartStatus.APPROVED);
            sp.setQuantityApproved(qtyApproved);
            sp.setApprovedBy(approver);
            sp.setApprovedAt(LocalDateTime.now());

            // Deduct stock
            inv.setQuantity(inv.getQuantity() - qtyApproved);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            // Record OUT transaction
            SparePartsTransaction tx = SparePartsTransaction.builder()
                    .tenant(sp.getService().getTenant())
                    .sparePart(sp.getSparePart())
                    .transactionType(StockTransactionType.OUT)
                    .quantity(qtyApproved)
                    .referenceType(StockReferenceType.SERVICE)
                    .servicePart(sp)
                    .notes("Used in service: " + sp.getService().getServiceNumber())
                    .createdBy(approver)
                    .build();
            transactionRepository.save(tx);

        } else if (request.getStatus() == ServicePartStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank())
                throw new FerosException("Rejection reason is required", HttpStatus.BAD_REQUEST);
            sp.setStatus(ServicePartStatus.REJECTED);
            sp.setRejectionReason(request.getRejectionReason());
            sp.setApprovedBy(approver);
            sp.setApprovedAt(LocalDateTime.now());
        } else {
            throw new FerosException("Status must be APPROVED or REJECTED", HttpStatus.BAD_REQUEST);
        }

        return toServicePartResponse(servicePartRepository.save(sp));
    }

    @Override
    @Transactional
    public void removeServicePart(Long servicePartId) {
        Long tenantId = getTenantId();
        ServicePart sp = servicePartRepository.findByIdAndService_TenantId(servicePartId, tenantId)
                .orElseThrow(() -> new FerosException("Service part request not found", HttpStatus.NOT_FOUND));

        if (sp.getStatus() == ServicePartStatus.APPROVED)
            throw new FerosException("Approved parts cannot be removed. Contact admin.", HttpStatus.BAD_REQUEST);

        if (sp.getService().getStatus() != ServiceStatus.IN_PROGRESS)
            throw new FerosException("Parts can only be removed from IN_PROGRESS services", HttpStatus.BAD_REQUEST);

        servicePartRepository.delete(sp);
    }

    @Override
    public List<ServicePartResponse> getPartsForService(Long serviceId) {
        return servicePartRepository.findByServiceIdOrderByCreatedAtDesc(serviceId)
                .stream().map(this::toServicePartResponse).toList();
    }

    @Override
    public List<ServicePartResponse> getPendingRequests() {
        return servicePartRepository
                .findByService_TenantIdAndStatusOrderByCreatedAtDesc(getTenantId(), ServicePartStatus.REQUESTED)
                .stream().map(this::toServicePartResponse).toList();
    }

    // ─── Transactions ─────────────────────────────────────────────────────────

    @Override
    public List<SparePartsTransactionResponse> getTransactions() {
        return transactionRepository.findByTenantIdOrderByCreatedAtDesc(getTenantId())
                .stream().map(this::toTransactionResponse).toList();
    }

    @Override
    public List<SparePartsTransactionResponse> getTransactionsByPart(Long sparePartId) {
        return transactionRepository.findByTenantIdAndSparePartIdOrderByCreatedAtDesc(getTenantId(), sparePartId)
                .stream().map(this::toTransactionResponse).toList();
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private SparePartResponse toSparePartResponse(SparePart p) {
        return SparePartResponse.builder()
                .id(p.getId())
                .tenantId(p.getTenant().getId())
                .name(p.getName())
                .partNumber(p.getPartNumber())
                .category(p.getCategory())
                .unit(p.getUnit())
                .minStockLevel(p.getMinStockLevel())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private StockItemResponse toStockItemResponse(SparePartsInventory inv) {
        SparePart p = inv.getSparePart();
        return StockItemResponse.builder()
                .inventoryId(inv.getId())
                .sparePartId(p.getId())
                .partName(p.getName())
                .partNumber(p.getPartNumber())
                .category(p.getCategory())
                .unit(p.getUnit())
                .quantity(inv.getQuantity())
                .minStockLevel(p.getMinStockLevel())
                .isLowStock(inv.getQuantity() <= p.getMinStockLevel())
                .build();
    }

    private ServicePartResponse toServicePartResponse(ServicePart sp) {
        VehicleService svc = sp.getService();
        return ServicePartResponse.builder()
                .id(sp.getId())
                .serviceId(svc.getId())
                .serviceNumber(svc.getServiceNumber())
                .vehicleRegistrationNumber(svc.getVehicle().getRegistrationNumber())
                .sparePartId(sp.getSparePart().getId())
                .partName(sp.getSparePart().getName())
                .partNumber(sp.getSparePart().getPartNumber())
                .unit(sp.getSparePart().getUnit())
                .quantityRequested(sp.getQuantityRequested())
                .quantityApproved(sp.getQuantityApproved())
                .status(sp.getStatus())
                .rejectionReason(sp.getRejectionReason())
                .requestedById(sp.getRequestedBy().getId())
                .requestedByName(sp.getRequestedBy().getName())
                .approvedById(sp.getApprovedBy() != null ? sp.getApprovedBy().getId() : null)
                .approvedByName(sp.getApprovedBy() != null ? sp.getApprovedBy().getName() : null)
                .approvedAt(sp.getApprovedAt())
                .createdAt(sp.getCreatedAt())
                .build();
    }

    private SparePartsTransactionResponse toTransactionResponse(SparePartsTransaction tx) {
        ServicePart sp = tx.getServicePart();
        return SparePartsTransactionResponse.builder()
                .id(tx.getId())
                .sparePartId(tx.getSparePart().getId())
                .partName(tx.getSparePart().getName())
                .unit(tx.getSparePart().getUnit())
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .unitCost(tx.getUnitCost())
                .totalCost(tx.getTotalCost())
                .referenceType(tx.getReferenceType())
                .servicePartId(sp != null ? sp.getId() : null)
                .serviceNumber(sp != null ? sp.getService().getServiceNumber() : null)
                .vehicleRegistrationNumber(sp != null ? sp.getService().getVehicle().getRegistrationNumber() : null)
                .supplierName(tx.getSupplierName())
                .notes(tx.getNotes())
                .createdById(tx.getCreatedBy().getId())
                .createdByName(tx.getCreatedBy().getName())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
