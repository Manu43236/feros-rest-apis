package com.feros.api.service.impl;

import com.feros.api.dto.request.CreateLrRequest;
import com.feros.api.dto.request.LrChargeRequest;
import com.feros.api.dto.request.LrCheckpostRequest;
import com.feros.api.dto.request.UpdateLrRequest;
import com.feros.api.dto.response.LrChargeResponse;
import com.feros.api.dto.response.LrCheckpostResponse;
import com.feros.api.dto.response.LrResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.ChargeType;
import com.feros.api.enums.LrStatus;
import com.feros.api.enums.OrderStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.LrService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LrServiceImpl implements LrService {

    private final LrRepository lrRepository;
    private final LrCheckpostRepository lrCheckpostRepository;
    private final LrChargeRepository lrChargeRepository;
    private final TenantRepository tenantRepository;
    private final OrderRepository orderRepository;
    private final OrderVehicleAllocationRepository vehicleAllocationRepository;
    private final ChargeTypeRepository chargeTypeRepository;
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

    private String generateLrNumber(Tenant tenant) {
        return NumberUtil.generate(tenant.getPrefix(), tenant.getId(), NumberUtil.Type.LR);
    }

    @Override
    @Transactional
    public LrResponse createLr(CreateLrRequest request) {
        Long tenantId = getCurrentTenantId();
        Tenant tenant = getCurrentTenant();

        if (lrRepository.existsByVehicleAllocationId(request.getVehicleAllocationId())) {
            throw new FerosException("LR already created for this vehicle allocation",
                    HttpStatus.CONFLICT);
        }

        OrderVehicleAllocation allocation = vehicleAllocationRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getVehicleAllocationId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle allocation not found",
                        HttpStatus.NOT_FOUND));

        Order order = allocation.getOrder();
        Vehicle vehicle = allocation.getVehicle();

        Lr lr = Lr.builder()
                .tenant(tenant)
                .lrNumber(generateLrNumber(tenant))
                .order(order)
                .vehicleAllocation(allocation)
                .lrDate(request.getLrDate() != null ? request.getLrDate() : LocalDate.now())
                .vehicleCapacity(vehicle.getCapacityInTons())
                .allocatedWeight(allocation.getAllocatedWeight())
                .loadedWeight(request.getLoadedWeight())
                .loadedAt(request.getLoadedAt())
                .lrStatus(LrStatus.CREATED)
                .remarks(request.getRemarks())
                .createdBy(getCurrentUser())
                .isActive(true)
                .build();

        // Update vehicle allocation status
        allocation.setAllocationStatus(VehicleAllocationStatus.LR_CREATED);
        vehicleAllocationRepository.save(allocation);

        return mapToLrResponse(lrRepository.save(lr));
    }

    @Override
    public LrResponse getLrById(Long id) {
        return mapToLrResponse(lrRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<LrResponse> getAllLrs() {
        return lrRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToLrResponse).toList();
    }

    @Override
    public List<LrResponse> getLrsByOrder(Long orderId) {
        return lrRepository.findByOrderIdAndIsActiveTrue(orderId)
                .stream().map(this::mapToLrResponse).toList();
    }

    @Override
    @Transactional
    public LrResponse updateLr(Long id, UpdateLrRequest request) {
        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        if (request.getLoadedWeight() != null) lr.setLoadedWeight(request.getLoadedWeight());
        if (request.getLoadedAt() != null) lr.setLoadedAt(request.getLoadedAt());
        if (request.getDeliveredWeight() != null) lr.setDeliveredWeight(request.getDeliveredWeight());
        if (request.getDeliveredAt() != null) lr.setDeliveredAt(request.getDeliveredAt());
        if (request.getRemarks() != null) lr.setRemarks(request.getRemarks());

        if (request.getLrStatus() != null) {
            lr.setLrStatus(request.getLrStatus());

            // Sync vehicle allocation status
            OrderVehicleAllocation allocation = lr.getVehicleAllocation();
            Order order = lr.getOrder();

            if (request.getLrStatus() == LrStatus.IN_TRANSIT) {
                allocation.setAllocationStatus(VehicleAllocationStatus.IN_TRANSIT);
                // At least one vehicle is on the road → order is IN_TRANSIT
                order.setOrderStatus(OrderStatus.IN_TRANSIT);
                orderRepository.save(order);

            } else if (request.getLrStatus() == LrStatus.DELIVERED) {
                allocation.setAllocationStatus(VehicleAllocationStatus.DELIVERED);

                // Update order fulfilled weight
                if (request.getDeliveredWeight() != null) {
                    order.setTotalWeightFulfilled(
                        order.getTotalWeightFulfilled().add(request.getDeliveredWeight()));
                }

                // Recalculate order status based on all sibling LRs for this order.
                // Current LR is already set to DELIVERED in memory — check all others in DB.
                List<Lr> siblingLrs = lrRepository.findByOrderIdAndIsActiveTrue(order.getId())
                        .stream()
                        .filter(l -> !l.getId().equals(lr.getId())
                                  && l.getLrStatus() != LrStatus.CANCELLED)
                        .toList();

                boolean allSiblingsDelivered = siblingLrs.stream()
                        .allMatch(l -> l.getLrStatus() == LrStatus.DELIVERED);

                if (allSiblingsDelivered) {
                    // Current LR + all others are DELIVERED → fully done
                    order.setOrderStatus(OrderStatus.DELIVERED);
                } else {
                    // Some still in progress
                    order.setOrderStatus(OrderStatus.PARTIALLY_DELIVERED);
                }
                orderRepository.save(order);
            }

            vehicleAllocationRepository.save(allocation);
        }

        return mapToLrResponse(lrRepository.save(lr));
    }

    @Override
    @Transactional
    public LrCheckpostResponse addCheckpost(Long lrId, LrCheckpostRequest request) {
        Long tenantId = getCurrentTenantId();
        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenantId)
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        LrCheckpost checkpost = LrCheckpost.builder()
                .tenant(getCurrentTenant())
                .lr(lr)
                .checkpostName(request.getCheckpostName())
                .location(request.getLocation())
                .fineAmount(request.getFineAmount())
                .fineReceiptNumber(request.getFineReceiptNumber())
                .finePaidAt(request.getFinePaidAt())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        return mapToCheckpostResponse(lrCheckpostRepository.save(checkpost));
    }

    @Override
    public List<LrCheckpostResponse> getCheckposts(Long lrId) {
        return lrCheckpostRepository.findByLrIdAndIsActiveTrue(lrId)
                .stream().map(this::mapToCheckpostResponse).toList();
    }

    @Override
    @Transactional
    public LrChargeResponse addCharge(Long lrId, LrChargeRequest request) {
        Long tenantId = getCurrentTenantId();
        Lr lr = lrRepository.findByIdAndTenantIdAndIsActiveTrue(lrId, tenantId)
                .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));

        ChargeType chargeType = chargeTypeRepository
                .findByIdAndTenantId(request.getChargeTypeId(), tenantId)
                .orElseThrow(() -> new FerosException("Charge type not found", HttpStatus.NOT_FOUND));

        LrCharge charge = LrCharge.builder()
                .tenant(getCurrentTenant())
                .lr(lr)
                .chargeType(chargeType)
                .amount(request.getAmount())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        return mapToChargeResponse(lrChargeRepository.save(charge));
    }

    @Override
    public List<LrChargeResponse> getCharges(Long lrId) {
        return lrChargeRepository.findByLrIdAndIsActiveTrue(lrId)
                .stream().map(this::mapToChargeResponse).toList();
    }

    // ===================== MAPPERS =====================
    private LrResponse mapToLrResponse(Lr lr) {
        Vehicle vehicle = lr.getVehicleAllocation().getVehicle();
        return LrResponse.builder()
                .id(lr.getId())
                .tenantId(lr.getTenant().getId())
                .lrNumber(lr.getLrNumber())
                .orderId(lr.getOrder().getId())
                .orderNumber(lr.getOrder().getOrderNumber())
                .vehicleAllocationId(lr.getVehicleAllocation().getId())
                .vehicleId(vehicle.getId())
                .vehicleRegistrationNumber(vehicle.getRegistrationNumber())
                .vehicleTypeName(vehicle.getVehicleType() != null ?
                        vehicle.getVehicleType().getName() : null)
                .clientId(lr.getOrder().getClient().getId())
                .clientName(lr.getOrder().getClient().getClientName())
                .lrDate(lr.getLrDate())
                .vehicleCapacity(lr.getVehicleCapacity())
                .allocatedWeight(lr.getAllocatedWeight())
                .loadedWeight(lr.getLoadedWeight())
                .overloadWeight(lr.getOverloadWeight())
                .deliveredWeight(lr.getDeliveredWeight())
                .weightVariance(lr.getWeightVariance())
                .isOverloaded(lr.getIsOverloaded())
                .loadedAt(lr.getLoadedAt())
                .deliveredAt(lr.getDeliveredAt())
                .lrStatus(lr.getLrStatus())
                .remarks(lr.getRemarks())
                .checkposts(lrCheckpostRepository.findByLrIdAndIsActiveTrue(lr.getId())
                        .stream().map(this::mapToCheckpostResponse).toList())
                .charges(lrChargeRepository.findByLrIdAndIsActiveTrue(lr.getId())
                        .stream().map(this::mapToChargeResponse).toList())
                .createdById(lr.getCreatedBy().getId())
                .createdByName(lr.getCreatedBy().getName())
                .isActive(lr.getIsActive())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt())
                .build();
    }

    private LrCheckpostResponse mapToCheckpostResponse(LrCheckpost c) {
        return LrCheckpostResponse.builder()
                .id(c.getId())
                .checkpostName(c.getCheckpostName())
                .location(c.getLocation())
                .fineAmount(c.getFineAmount())
                .fineReceiptNumber(c.getFineReceiptNumber())
                .finePaidAt(c.getFinePaidAt())
                .remarks(c.getRemarks())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private LrChargeResponse mapToChargeResponse(LrCharge c) {
        return LrChargeResponse.builder()
                .id(c.getId())
                .chargeTypeId(c.getChargeType().getId())
                .chargeTypeName(c.getChargeType().getName())
                .amount(c.getAmount())
                .remarks(c.getRemarks())
                .createdAt(c.getCreatedAt())
                .build();
    }
}