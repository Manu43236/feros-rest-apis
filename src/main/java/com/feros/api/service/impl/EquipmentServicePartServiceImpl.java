package com.feros.api.service.impl;

import com.feros.api.dto.request.ServicePartApprovalRequest;
import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.EquipmentServicePartResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.SparePart;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.StockTransactionType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.EquipmentServicePartService;
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
public class EquipmentServicePartServiceImpl implements EquipmentServicePartService {

    private final EquipmentServicePartRepository partRepository;
    private final EquipmentServiceRepository serviceRepository;
    private final EquipmentServiceTaskRepository taskRepository;
    private final SparePartRepository sparePartRepository;
    private final SparePartsInventoryRepository inventoryRepository;
    private final SparePartsTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private Long tenantId() { return SecurityUtil.getCurrentTenantId(); }

    private User currentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public EquipmentServicePartResponse requestPart(Long equipmentId, Long serviceId, ServicePartRequest request) {
        Long tid = tenantId();
        EquipmentServiceRecord service = serviceRepository.findByIdAndTenantIdAndIsActiveTrue(serviceId, tid)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        SparePart part = sparePartRepository.findByIdAndTenantIdAndIsActiveTrue(request.getSparePartId(), tid)
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));

        EquipmentServiceTask task = null;
        if (request.getTaskId() != null) {
            task = taskRepository.findById(request.getTaskId()).orElse(null);
        }

        EquipmentServicePart esp = EquipmentServicePart.builder()
                .service(service)
                .serviceTask(task)
                .sparePart(part)
                .quantityRequested(request.getQuantityRequested())
                .status(ServicePartStatus.REQUESTED)
                .requestedBy(currentUser())
                .isActive(true)
                .build();
        return toResponse(partRepository.save(esp));
    }

    @Override
    public List<EquipmentServicePartResponse> getServiceParts(Long serviceId) {
        return partRepository.findByServiceIdAndIsActiveTrue(serviceId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<EquipmentServicePartResponse> getPending() {
        return partRepository.findByStatusAndService_TenantIdAndIsActiveTrue(ServicePartStatus.REQUESTED, tenantId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void removePart(Long partId) {
        EquipmentServicePart esp = partRepository.findByIdAndService_TenantId(partId, tenantId())
                .orElseThrow(() -> new FerosException("Part request not found", HttpStatus.NOT_FOUND));
        if (esp.getStatus() != ServicePartStatus.REQUESTED)
            throw new FerosException("Only pending part requests can be removed", HttpStatus.BAD_REQUEST);
        esp.setIsActive(false);
        partRepository.save(esp);
    }

    @Override
    @Transactional
    public EquipmentServicePartResponse approvePart(Long partId, ServicePartApprovalRequest request) {
        Long tid = tenantId();
        EquipmentServicePart esp = partRepository.findByIdAndService_TenantId(partId, tid)
                .orElseThrow(() -> new FerosException("Part request not found", HttpStatus.NOT_FOUND));

        if (esp.getStatus() != ServicePartStatus.REQUESTED)
            throw new FerosException("Only REQUESTED parts can be approved/rejected", HttpStatus.BAD_REQUEST);

        User approver = currentUser();

        if (request.getStatus() == ServicePartStatus.APPROVED) {
            int qty = request.getQuantityApproved() != null ? request.getQuantityApproved() : esp.getQuantityRequested();

            SparePartsInventory inv = inventoryRepository
                    .findByTenantIdAndSparePartId(tid, esp.getSparePart().getId())
                    .orElseThrow(() -> new FerosException("No inventory record for this part", HttpStatus.NOT_FOUND));

            if (inv.getQuantity() < qty)
                throw new FerosException("Insufficient stock. Only " + inv.getQuantity() + " "
                        + esp.getSparePart().getName() + " available.", HttpStatus.BAD_REQUEST);

            esp.setStatus(ServicePartStatus.APPROVED);
            esp.setQuantityApproved(qty);
            esp.setApprovedBy(approver);
            esp.setApprovedAt(LocalDateTime.now());

            // Deduct from the SHARED inventory
            inv.setQuantity(inv.getQuantity() - qty);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            // Record an OUT transaction in the shared ledger (servicePart left null — this is an equipment usage)
            BigDecimal unitCost = transactionRepository
                    .findTopByTenantIdAndSparePartIdAndReferenceTypeOrderByCreatedAtDesc(
                            tid, esp.getSparePart().getId(), StockReferenceType.PURCHASE)
                    .map(SparePartsTransaction::getUnitCost).orElse(BigDecimal.ZERO);
            SparePartsTransaction tx = SparePartsTransaction.builder()
                    .tenant(esp.getService().getTenant())
                    .sparePart(esp.getSparePart())
                    .transactionType(StockTransactionType.OUT)
                    .quantity(qty)
                    .unitCost(unitCost)
                    .totalCost(unitCost.multiply(BigDecimal.valueOf(qty)))
                    .referenceType(StockReferenceType.SERVICE)
                    .notes("Used in equipment service: " + esp.getService().getServiceNumber())
                    .createdBy(approver)
                    .build();
            transactionRepository.save(tx);

        } else if (request.getStatus() == ServicePartStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank())
                throw new FerosException("Rejection reason is required", HttpStatus.BAD_REQUEST);
            esp.setStatus(ServicePartStatus.REJECTED);
            esp.setRejectionReason(request.getRejectionReason());
        } else {
            throw new FerosException("Status must be APPROVED or REJECTED", HttpStatus.BAD_REQUEST);
        }

        return toResponse(partRepository.save(esp));
    }

    private EquipmentServicePartResponse toResponse(EquipmentServicePart p) {
        SparePart sp = p.getSparePart();
        Integer stock = inventoryRepository
                .findByTenantIdAndSparePartId(p.getService().getTenant().getId(), sp.getId())
                .map(SparePartsInventory::getQuantity).orElse(0);
        var model = p.getService().getEquipment().getEquipmentType().getModel();
        String machineName = model.getMake().getName() + " " + model.getName();
        return EquipmentServicePartResponse.builder()
                .id(p.getId())
                .taskId(p.getServiceTask() != null ? p.getServiceTask().getId() : null)
                .sparePartId(sp.getId())
                .sparePartName(sp.getName())
                .partNumber(sp.getPartNumber())
                .unit(sp.getUnit())
                .quantityRequested(p.getQuantityRequested())
                .quantityApproved(p.getQuantityApproved())
                .status(p.getStatus())
                .rejectionReason(p.getRejectionReason())
                .requestedByName(p.getRequestedBy() != null ? p.getRequestedBy().getName() : null)
                .createdAt(p.getCreatedAt())
                .availableStock(stock)
                .serviceNumber(p.getService().getServiceNumber())
                .equipmentName(machineName)
                .build();
    }
}
