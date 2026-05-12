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
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.LrService;
import com.feros.api.entity.VehicleMeterReading;
import com.feros.api.enums.MeterReadingType;
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
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargeTypeRepository chargeTypeRepository;
    private final UserRepository userRepository;
    private final VehicleMeterReadingRepository vehicleMeterReadingRepository;

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

    private void setVehicleStatus(com.feros.api.entity.Vehicle vehicle, VehicleStatusType type) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(type)
                .ifPresent(status -> {
                    vehicle.setCurrentStatus(status);
                    vehicleRepository.save(vehicle);
                });
    }

    private String generateLrNumber(Tenant tenant) {
        return NumberUtil.generate(tenant.getPrefix(), tenant.getId(), NumberUtil.Type.LR);
    }

    @Override
    @Transactional
    public LrResponse createLr(CreateLrRequest request) {
        Long tenantId = getCurrentTenantId();
        Tenant tenant = getCurrentTenant();

        if (lrRepository.existsByVehicleAllocationIdAndLrStatusNot(
                request.getVehicleAllocationId(), LrStatus.CANCELLED)) {
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
        Long tenantId = getCurrentTenantId();
        String role = SecurityUtil.getCurrentRole();
        if ("DRIVER".equals(role) || "CLEANER".equals(role)) {
            return lrRepository.findByTenantIdAndDriverUserId(tenantId, SecurityUtil.getCurrentUserId())
                    .stream().map(this::mapToLrResponse).toList();
        }
        if ("SUPERVISOR".equals(role)) {
            return lrRepository.findByTenantIdAndCreatedByIdAndIsActiveTrue(tenantId, SecurityUtil.getCurrentUserId())
                    .stream().map(this::mapToLrResponse).toList();
        }
        return lrRepository.findByTenantIdAndIsActiveTrue(tenantId)
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
        if (request.getDeliveredWeight() != null) {
            java.math.BigDecimal effectiveLoaded = request.getLoadedWeight() != null
                    ? request.getLoadedWeight() : lr.getLoadedWeight();
            if (effectiveLoaded != null && request.getDeliveredWeight().compareTo(effectiveLoaded) > 0) {
                throw new FerosException(
                    "Delivered weight (" + request.getDeliveredWeight() + "T) cannot exceed loaded weight (" + effectiveLoaded + "T)",
                    HttpStatus.BAD_REQUEST);
            }
            lr.setDeliveredWeight(request.getDeliveredWeight());
        }
        if (request.getDeliveredAt() != null) lr.setDeliveredAt(request.getDeliveredAt());
        if (request.getRemarks() != null) lr.setRemarks(request.getRemarks());

        if (request.getLrStatus() != null) {
            lr.setLrStatus(request.getLrStatus());

            // Sync vehicle allocation status
            OrderVehicleAllocation allocation = lr.getVehicleAllocation();
            Order order = lr.getOrder();

            if (request.getLrStatus() == LrStatus.WEIGHT_LOADED) {
                // Weight recorded by supervisor — no order/vehicle/staff status change yet.
                // Driver will explicitly start the trip from mobile (→ IN_TRANSIT).

            } else if (request.getLrStatus() == LrStatus.IN_TRANSIT) {
                // ODM validation — start odometer required
                if (request.getStartOdometer() == null) {
                    throw new FerosException("Start odometer reading is required to start the trip",
                            HttpStatus.BAD_REQUEST);
                }
                com.feros.api.entity.Vehicle odmVehicle = allocation.getVehicle();
                if (odmVehicle.getCurrentOdometerReading() != null &&
                    request.getStartOdometer().compareTo(odmVehicle.getCurrentOdometerReading()) < 0) {
                    throw new FerosException(
                        "Start odometer (" + request.getStartOdometer() + " km) cannot be less than last recorded reading (" + odmVehicle.getCurrentOdometerReading() + " km)",
                        HttpStatus.BAD_REQUEST);
                }
                VehicleMeterReading startReading = VehicleMeterReading.builder()
                        .tenant(lr.getTenant())
                        .vehicle(odmVehicle)
                        .readingKm(request.getStartOdometer())
                        .readingType(MeterReadingType.TRIP_START)
                        .lr(lr)
                        .photoUrl(request.getStartOdometerPhotoUrl())
                        .recordedBy(getCurrentUser())
                        .recordedAt(java.time.LocalDateTime.now())
                        .isActive(true)
                        .build();
                vehicleMeterReadingRepository.save(startReading);
                odmVehicle.setCurrentOdometerReading(request.getStartOdometer());
                vehicleRepository.save(odmVehicle);

                allocation.setAllocationStatus(VehicleAllocationStatus.IN_TRANSIT);
                order.setOrderStatus(OrderStatus.IN_TRANSIT);
                orderRepository.save(order);
                setVehicleStatus(allocation.getVehicle(), VehicleStatusType.ON_TRIP);

                List<OrderStaffAllocation> staffAllocations =
                        staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(allocation.getId());
                for (OrderStaffAllocation sa : staffAllocations) {
                    if (sa.getAllocationStatus() == StaffAllocationStatus.ALLOCATED) {
                        sa.setAllocationStatus(StaffAllocationStatus.IN_TRANSIT);
                        sa.setActualStartDate(LocalDate.now());
                    }
                }
                staffAllocationRepository.saveAll(staffAllocations);

            } else if (request.getLrStatus() == LrStatus.CANCELLED) {
                // Revert allocation back to ALLOCATED — vehicle is still assigned to order, just no LR
                allocation.setAllocationStatus(VehicleAllocationStatus.ALLOCATED);
                // Vehicle goes back to ASSIGNED (still allocated to order)
                setVehicleStatus(allocation.getVehicle(), VehicleStatusType.ASSIGNED);

                // Revert staff on this vehicle allocation back to ALLOCATED
                List<OrderStaffAllocation> staffAllocations =
                        staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(allocation.getId());
                for (OrderStaffAllocation sa : staffAllocations) {
                    if (sa.getAllocationStatus() == StaffAllocationStatus.IN_TRANSIT) {
                        sa.setAllocationStatus(StaffAllocationStatus.ALLOCATED);
                        sa.setActualStartDate(null);
                    }
                }
                staffAllocationRepository.saveAll(staffAllocations);

            } else if (request.getLrStatus() == LrStatus.DELIVERED) {
                // ODM validation — end odometer required
                if (request.getEndOdometer() == null) {
                    throw new FerosException("End odometer reading is required to complete the trip",
                            HttpStatus.BAD_REQUEST);
                }
                java.util.Optional<VehicleMeterReading> startReadingOpt =
                        vehicleMeterReadingRepository
                                .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                                        lr.getId(), MeterReadingType.TRIP_START);
                java.math.BigDecimal startKm = startReadingOpt
                        .map(VehicleMeterReading::getReadingKm)
                        .orElse(java.math.BigDecimal.ZERO);
                if (request.getEndOdometer().compareTo(startKm) <= 0) {
                    throw new FerosException(
                        "End odometer (" + request.getEndOdometer() + " km) must be greater than start odometer (" + startKm + " km)",
                        HttpStatus.BAD_REQUEST);
                }
                com.feros.api.entity.Vehicle endVehicle = allocation.getVehicle();
                VehicleMeterReading endReading = VehicleMeterReading.builder()
                        .tenant(lr.getTenant())
                        .vehicle(endVehicle)
                        .readingKm(request.getEndOdometer())
                        .readingType(MeterReadingType.TRIP_END)
                        .lr(lr)
                        .photoUrl(request.getEndOdometerPhotoUrl())
                        .recordedBy(getCurrentUser())
                        .recordedAt(java.time.LocalDateTime.now())
                        .isActive(true)
                        .build();
                vehicleMeterReadingRepository.save(endReading);
                endVehicle.setCurrentOdometerReading(request.getEndOdometer());
                vehicleRepository.save(endVehicle);

                allocation.setAllocationStatus(VehicleAllocationStatus.DELIVERED);

                // Update order fulfilled weight
                if (request.getDeliveredWeight() != null) {
                    order.setTotalWeightFulfilled(
                        order.getTotalWeightFulfilled().add(request.getDeliveredWeight()));
                }

                // Recalculate order status based on all vehicle allocations for this order.
                // Current allocation is already set to DELIVERED in memory — check all others in DB.
                List<OrderVehicleAllocation> allAllocations =
                        vehicleAllocationRepository.findByOrderIdAndIsActiveTrue(order.getId());

                // All non-cancelled allocations delivered?
                boolean allAllocationsDelivered = allAllocations.stream()
                        .filter(va -> va.getAllocationStatus() != VehicleAllocationStatus.CANCELLED)
                        .allMatch(va ->
                                va.getId().equals(allocation.getId())
                                || va.getAllocationStatus() == VehicleAllocationStatus.DELIVERED);

                // Total weight assigned across all non-cancelled allocations
                java.math.BigDecimal totalAllocated = allAllocations.stream()
                        .filter(va -> va.getAllocationStatus() != VehicleAllocationStatus.CANCELLED)
                        .map(OrderVehicleAllocation::getAllocatedWeight)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                boolean allWeightAllocated = totalAllocated.compareTo(order.getTotalWeight()) >= 0;

                // Always complete staff for this vehicle allocation — their trip is done
                List<OrderStaffAllocation> staffAllocations =
                        staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(allocation.getId());
                for (OrderStaffAllocation sa : staffAllocations) {
                    sa.setAllocationStatus(StaffAllocationStatus.COMPLETED);
                    sa.setActualEndDate(LocalDate.now());
                }
                staffAllocationRepository.saveAll(staffAllocations);

                // Set actual delivery date and free the vehicle regardless
                allocation.setActualDeliveryDate(LocalDate.now());
                setVehicleStatus(allocation.getVehicle(), VehicleStatusType.AVAILABLE);

                if (allAllocationsDelivered && allWeightAllocated) {
                    order.setOrderStatus(OrderStatus.DELIVERED);
                } else {
                    order.setOrderStatus(OrderStatus.PARTIALLY_DELIVERED);
                }
                orderRepository.save(order);
            }

            vehicleAllocationRepository.save(allocation);
        }

        lrRepository.save(lr);
        // Re-fetch to pick up DB-generated columns (weight_variance, is_overloaded, overload_weight)
        return mapToLrResponse(lrRepository.findById(lr.getId()).orElse(lr));
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

    @Override
    @Transactional
    public void deleteCheckpost(Long checkpostId) {
        LrCheckpost checkpost = lrCheckpostRepository.findById(checkpostId)
                .orElseThrow(() -> new FerosException("Checkpost not found", HttpStatus.NOT_FOUND));
        checkpost.setIsActive(false);
        lrCheckpostRepository.save(checkpost);
    }

    @Override
    @Transactional
    public void deleteCharge(Long chargeId) {
        LrCharge charge = lrChargeRepository.findById(chargeId)
                .orElseThrow(() -> new FerosException("Charge not found", HttpStatus.NOT_FOUND));
        charge.setIsActive(false);
        lrChargeRepository.save(charge);
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
                .fromCity(lr.getOrder().getSourceCity() != null ? lr.getOrder().getSourceCity().getName() : null)
                .toCity(lr.getOrder().getDestinationCity() != null ? lr.getOrder().getDestinationCity().getName() : null)
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
                .startOdometer(vehicleMeterReadingRepository
                        .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                                lr.getId(), MeterReadingType.TRIP_START)
                        .map(VehicleMeterReading::getReadingKm).orElse(null))
                .endOdometer(vehicleMeterReadingRepository
                        .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                                lr.getId(), MeterReadingType.TRIP_END)
                        .map(VehicleMeterReading::getReadingKm).orElse(null))
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