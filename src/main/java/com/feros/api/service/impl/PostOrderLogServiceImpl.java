package com.feros.api.service.impl;

import com.feros.api.dto.request.PolLrRequest;
import com.feros.api.dto.request.PostOrderLogRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.City;
import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.State;
import com.feros.api.enums.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.PostOrderLogService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostOrderLogServiceImpl implements PostOrderLogService {

    private final OrderRepository orderRepository;
    private final OrderVehicleAllocationRepository vehicleAllocationRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final LrRepository lrRepository;
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AttendanceRepository attendanceRepository;
    private final NumberGeneratorService numberGenerator;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createPostOrderLog(PostOrderLogRequest request) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        User createdBy = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        if (request.getOrderDate().isAfter(TimeUtil.today())) {
            throw new FerosException("POL order date cannot be in the future", HttpStatus.BAD_REQUEST);
        }

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenant.getId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        MaterialType materialType = resolveMaterialType(request, tenant);

        City sourceCity = cityRepository.findById(request.getSourceCityId())
                .orElseThrow(() -> new FerosException("Source city not found", HttpStatus.NOT_FOUND));
        State sourceState = stateRepository.findById(request.getSourceStateId())
                .orElseThrow(() -> new FerosException("Source state not found", HttpStatus.NOT_FOUND));
        City destCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new FerosException("Destination city not found", HttpStatus.NOT_FOUND));
        State destState = stateRepository.findById(request.getDestinationStateId())
                .orElseThrow(() -> new FerosException("Destination state not found", HttpStatus.NOT_FOUND));

        // Pre-validate all driver/cleaner attendance before creating anything
        List<Long> presentUserIds = attendanceRepository.findUserIdsWithAttendanceOnDate(
                tenant.getId(), request.getOrderDate(),
                List.of(AttendanceApprovalStatus.APPROVED, AttendanceApprovalStatus.PENDING));

        for (PolLrRequest lrReq : request.getLrs()) {
            if (!presentUserIds.contains(lrReq.getDriverId())) {
                User driver = userRepository.findById(lrReq.getDriverId())
                        .orElseThrow(() -> new FerosException("Driver not found: " + lrReq.getDriverId(), HttpStatus.NOT_FOUND));
                throw new FerosException("Driver '" + driver.getName() + "' has no attendance on " + request.getOrderDate(), HttpStatus.BAD_REQUEST);
            }
            if (lrReq.getCleanerId() != null && !presentUserIds.contains(lrReq.getCleanerId())) {
                User cleaner = userRepository.findById(lrReq.getCleanerId())
                        .orElseThrow(() -> new FerosException("Cleaner not found: " + lrReq.getCleanerId(), HttpStatus.NOT_FOUND));
                throw new FerosException("Cleaner '" + cleaner.getName() + "' has no attendance on " + request.getOrderDate(), HttpStatus.BAD_REQUEST);
            }
        }

        Role driverRole = roleRepository.findByName(RoleName.DRIVER)
                .orElseThrow(() -> new FerosException("Driver role not found", HttpStatus.INTERNAL_SERVER_ERROR));
        Role cleanerRole = roleRepository.findByName(RoleName.CLEANER)
                .orElseThrow(() -> new FerosException("Cleaner role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        // Calculate total weight fulfilled from all LR rows
        BigDecimal totalFulfilled = request.getLrs().stream()
                .map(PolLrRequest::getAllocatedWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .tenant(tenant)
                .orderNumber(numberGenerator.generateFY(tenant.getId(), NumberUtil.Type.ORD))
                .orderDate(request.getOrderDate())
                .createdBy(createdBy)
                .client(client)
                .materialType(materialType)
                .totalWeight(request.getTotalWeight())
                .totalWeightFulfilled(totalFulfilled)
                .sourceAddress(request.getSourceAddress())
                .sourceCity(sourceCity)
                .sourceState(sourceState)
                .destinationAddress(request.getDestinationAddress())
                .destinationCity(destCity)
                .destinationState(destState)
                .freightRateType(request.getFreightRateType())
                .freightRate(request.getFreightRate())
                .billingOn(request.getBillingOn() != null ? request.getBillingOn() : BillingOn.LOADED_WEIGHT)
                .orderStatus(OrderStatus.DELIVERED)
                .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                .specialInstructions(request.getSpecialInstructions())
                .remarks(request.getRemarks())
                .isPol(true)
                .isActive(true)
                .build();

        if (request.getRouteId() != null) {
            order.setRoute(routeRepository.findByIdAndTenantId(request.getRouteId(), tenant.getId())
                    .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND)));
        }

        Order savedOrder = orderRepository.save(order);

        for (PolLrRequest lrReq : request.getLrs()) {
            Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(lrReq.getVehicleId(), tenant.getId())
                    .orElseThrow(() -> new FerosException("Vehicle not found: " + lrReq.getVehicleId(), HttpStatus.NOT_FOUND));
            User driver = userRepository.findByIdAndIsActiveTrue(lrReq.getDriverId())
                    .orElseThrow(() -> new FerosException("Driver not found: " + lrReq.getDriverId(), HttpStatus.NOT_FOUND));

            LocalDate lrDate = lrReq.getLrDate() != null ? lrReq.getLrDate() : request.getOrderDate();

            OrderVehicleAllocation allocation = OrderVehicleAllocation.builder()
                    .tenant(tenant)
                    .order(savedOrder)
                    .vehicle(vehicle)
                    .allocatedWeight(lrReq.getAllocatedWeight())
                    .actualLoadDate(lrDate)
                    .actualDeliveryDate(lrDate)
                    .allocationStatus(VehicleAllocationStatus.DELIVERED)
                    .allocatedBy(createdBy)
                    .isActive(true)
                    .build();

            OrderVehicleAllocation savedAllocation = vehicleAllocationRepository.save(allocation);

            staffAllocationRepository.save(OrderStaffAllocation.builder()
                    .tenant(tenant)
                    .order(savedOrder)
                    .vehicleAllocation(savedAllocation)
                    .user(driver)
                    .role(driverRole)
                    .actualStartDate(lrDate)
                    .actualEndDate(lrDate)
                    .allocationStatus(StaffAllocationStatus.COMPLETED)
                    .allocatedBy(createdBy)
                    .isActive(true)
                    .build());

            if (lrReq.getCleanerId() != null) {
                User cleaner = userRepository.findByIdAndIsActiveTrue(lrReq.getCleanerId())
                        .orElseThrow(() -> new FerosException("Cleaner not found: " + lrReq.getCleanerId(), HttpStatus.NOT_FOUND));
                staffAllocationRepository.save(OrderStaffAllocation.builder()
                        .tenant(tenant)
                        .order(savedOrder)
                        .vehicleAllocation(savedAllocation)
                        .user(cleaner)
                        .role(cleanerRole)
                        .actualStartDate(lrDate)
                        .actualEndDate(lrDate)
                        .allocationStatus(StaffAllocationStatus.COMPLETED)
                        .allocatedBy(createdBy)
                        .isActive(true)
                        .build());
            }

            Lr lr = Lr.builder()
                    .tenant(tenant)
                    .lrNumber(numberGenerator.generateFY(tenant.getId(), NumberUtil.Type.LR))
                    .paperLrNumber(lrReq.getPaperLrNumber())
                    .order(savedOrder)
                    .vehicleAllocation(savedAllocation)
                    .lrDate(lrDate)
                    .vehicleCapacity(lrReq.getVehicleCapacity())
                    .allocatedWeight(lrReq.getAllocatedWeight())
                    .loadedWeight(lrReq.getLoadedWeight())
                    .deliveredWeight(lrReq.getDeliveredWeight())
                    .loadedAt(lrDate.atStartOfDay())
                    .deliveredAt(lrDate.atStartOfDay())
                    .lrStatus(LrStatus.DELIVERED)
                    .driver(driver)
                    .ewayBillNumber(lrReq.getEwayBillNumber())
                    .ewayBillDate(lrReq.getEwayBillDate())
                    .ewayBillValidUpto(lrReq.getEwayBillValidUpto())
                    .remarks(lrReq.getRemarks())
                    .createdBy(createdBy)
                    .isActive(true)
                    .build();

            if (lrReq.getCleanerId() != null) {
                userRepository.findByIdAndIsActiveTrue(lrReq.getCleanerId())
                        .ifPresent(lr::setCleaner);
            }

            lrRepository.save(lr);
        }

        notificationService.sendToRoles(savedOrder.getTenant(),
                List.of(RoleName.SUPERVISOR, RoleName.OFFICE_STAFF, RoleName.ADMIN),
                NotificationType.ORDER_CREATED,
                "POL Order Created",
                "POL order " + savedOrder.getOrderNumber() + " created with " + request.getLrs().size() + " LR(s).",
                Map.of("type", "NEW_ORDER", "orderId", String.valueOf(savedOrder.getId())));

        return mapToOrderResponse(orderRepository.findById(savedOrder.getId()).orElse(savedOrder));
    }

    private MaterialType resolveMaterialType(PostOrderLogRequest request, Tenant tenant) {
        if (request.getMaterialTypeId() != null && request.getMaterialTypeId() > 0) {
            return materialTypeRepository.findById(request.getMaterialTypeId())
                    .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND));
        }
        if (request.getCustomMaterialName() != null && !request.getCustomMaterialName().isBlank()) {
            return materialTypeRepository.findByNameIgnoreCase(request.getCustomMaterialName())
                    .orElseGet(() -> {
                        MaterialType mt = new MaterialType();
                        mt.setName(request.getCustomMaterialName().trim());
                        mt.setIsActive(true);
                        return materialTypeRepository.save(mt);
                    });
        }
        throw new FerosException("Material type or custom material name is required", HttpStatus.BAD_REQUEST);
    }

    private OrderResponse mapToOrderResponse(Order o) {
        List<VehicleAllocationResponse> vehicleAllocations = new ArrayList<>();
        for (OrderVehicleAllocation a : vehicleAllocationRepository.findByOrderIdAndIsActiveTrue(o.getId())) {
            vehicleAllocations.add(VehicleAllocationResponse.builder()
                    .id(a.getId())
                    .vehicleId(a.getVehicle().getId())
                    .vehicleRegistrationNumber(a.getVehicle().getRegistrationNumber())
                    .allocatedWeight(a.getAllocatedWeight())
                    .allocationStatus(VehicleAllocationStatus.valueOf(a.getAllocationStatus().name()))
                    .build());
        }

        return OrderResponse.builder()
                .id(o.getId())
                .tenantId(o.getTenant().getId())
                .orderNumber(o.getOrderNumber())
                .orderDate(o.getOrderDate())
                .clientId(o.getClient().getId())
                .clientName(o.getClient().getClientName())
                .materialTypeId(o.getMaterialType().getId())
                .materialTypeName(o.getMaterialType().getName())
                .totalWeight(o.getTotalWeight())
                .totalWeightFulfilled(o.getTotalWeightFulfilled())
                .remainingWeight(o.getTotalWeight().subtract(o.getTotalWeightFulfilled()).setScale(2, java.math.RoundingMode.HALF_UP))
                .sourceAddress(o.getSourceAddress())
                .sourceCityId(o.getSourceCity().getId())
                .sourceCityName(o.getSourceCity().getName())
                .sourceStateId(o.getSourceState().getId())
                .sourceStateName(o.getSourceState().getName())
                .destinationAddress(o.getDestinationAddress())
                .destinationCityId(o.getDestinationCity().getId())
                .destinationCityName(o.getDestinationCity().getName())
                .destinationStateId(o.getDestinationState().getId())
                .destinationStateName(o.getDestinationState().getName())
                .routeId(o.getRoute() != null ? o.getRoute().getId() : null)
                .routeName(o.getRoute() != null ? o.getRoute().getName() : null)
                .freightRateType(o.getFreightRateType())
                .freightRate(o.getFreightRate())
                .billingOn(o.getBillingOn())
                .orderStatus(o.getOrderStatus())
                .orderPaymentStatus(o.getOrderPaymentStatus())
                .specialInstructions(o.getSpecialInstructions())
                .remarks(o.getRemarks())
                .createdById(o.getCreatedBy().getId())
                .createdByName(o.getCreatedBy().getName())
                .vehicleAllocations(vehicleAllocations)
                .isPol(o.getIsPol())
                .isActive(o.getIsActive())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
