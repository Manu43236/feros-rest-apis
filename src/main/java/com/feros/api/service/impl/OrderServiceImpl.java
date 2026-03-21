package com.feros.api.service.impl;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.City;
import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.Route;
import com.feros.api.entity.master.State;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.OrderService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderVehicleAllocationRepository vehicleAllocationRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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

    /** Resolves materialType from either a known ID or a free-text custom name ("Other"). */
    private MaterialType resolveMaterialType(OrderRequest request) {
        if (request.getMaterialTypeId() != null) {
            return materialTypeRepository.findById(request.getMaterialTypeId())
                    .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND));
        }
        if (request.getCustomMaterialName() != null && !request.getCustomMaterialName().isBlank()) {
            String name = request.getCustomMaterialName().trim();
            return materialTypeRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> materialTypeRepository.save(
                            MaterialType.builder().name(name).isActive(true).build()));
        }
        throw new FerosException("Material type is required", HttpStatus.BAD_REQUEST);
    }

    private String generateOrderNumber(Tenant tenant) {
        return NumberUtil.generate(tenant.getPrefix(), tenant.getId(), NumberUtil.Type.ORD);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Tenant tenant = getCurrentTenant();
        User createdBy = getCurrentUser();

        Client client = clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenant.getId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        MaterialType materialType = resolveMaterialType(request);

        City sourceCity = cityRepository.findById(request.getSourceCityId())
                .orElseThrow(() -> new FerosException("Source city not found", HttpStatus.NOT_FOUND));

        State sourceState = stateRepository.findById(request.getSourceStateId())
                .orElseThrow(() -> new FerosException("Source state not found", HttpStatus.NOT_FOUND));

        City destinationCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new FerosException("Destination city not found", HttpStatus.NOT_FOUND));

        State destinationState = stateRepository.findById(request.getDestinationStateId())
                .orElseThrow(() -> new FerosException("Destination state not found", HttpStatus.NOT_FOUND));

        Order order = Order.builder()
                .tenant(tenant)
                .orderNumber(generateOrderNumber(tenant))
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .createdBy(createdBy)
                .client(client)
                .materialType(materialType)
                .totalWeight(request.getTotalWeight())
                .totalWeightFulfilled(BigDecimal.ZERO)
                .sourceAddress(request.getSourceAddress())
                .sourceCity(sourceCity)
                .sourceState(sourceState)
                .destinationAddress(request.getDestinationAddress())
                .destinationCity(destinationCity)
                .destinationState(destinationState)
                .freightRateType(request.getFreightRateType())
                .freightRate(request.getFreightRate())
                .billingOn(request.getBillingOn() != null ? request.getBillingOn() : BillingOn.LOADED_WEIGHT)
                .orderStatus(OrderStatus.PENDING)
                .specialInstructions(request.getSpecialInstructions())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        if (request.getRouteId() != null)
            order.setRoute(routeRepository.findByIdAndTenantId(request.getRouteId(), tenant.getId())
                    .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND)));

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return mapToOrderResponse(orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToOrderResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new FerosException("Only PENDING orders can be updated", HttpStatus.BAD_REQUEST);
        }

        Client client = clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        MaterialType materialType = resolveMaterialType(request);

        City sourceCity = cityRepository.findById(request.getSourceCityId())
                .orElseThrow(() -> new FerosException("Source city not found", HttpStatus.NOT_FOUND));

        State sourceState = stateRepository.findById(request.getSourceStateId())
                .orElseThrow(() -> new FerosException("Source state not found", HttpStatus.NOT_FOUND));

        City destinationCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new FerosException("Destination city not found", HttpStatus.NOT_FOUND));

        State destinationState = stateRepository.findById(request.getDestinationStateId())
                .orElseThrow(() -> new FerosException("Destination state not found", HttpStatus.NOT_FOUND));

        order.setClient(client);
        order.setMaterialType(materialType);
        order.setTotalWeight(request.getTotalWeight());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setSourceAddress(request.getSourceAddress());
        order.setSourceCity(sourceCity);
        order.setSourceState(sourceState);
        order.setDestinationAddress(request.getDestinationAddress());
        order.setDestinationCity(destinationCity);
        order.setDestinationState(destinationState);
        order.setFreightRateType(request.getFreightRateType());
        order.setFreightRate(request.getFreightRate());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setRemarks(request.getRemarks());

        if (request.getBillingOn() != null) order.setBillingOn(request.getBillingOn());
        if (request.getRouteId() != null)
            order.setRoute(routeRepository.findByIdAndTenantId(request.getRouteId(), getCurrentTenantId())
                    .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND)));

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public VehicleAllocationResponse assignVehicle(Long orderId, AssignVehicleRequest request) {
        Long tenantId = getCurrentTenantId();

        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.CANCELLED ||
                order.getOrderStatus() == OrderStatus.DELIVERED ||
                order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new FerosException("Cannot assign vehicle to a " +
                    order.getOrderStatus() + " order", HttpStatus.BAD_REQUEST);
        }

        if (vehicleAllocationRepository.existsByOrderIdAndVehicleIdAndIsActiveTrue(
                orderId, request.getVehicleId())) {
            throw new FerosException("Vehicle already assigned to this order", HttpStatus.CONFLICT);
        }

        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        // Validate allocated weight doesn't exceed remaining weight
        BigDecimal remaining = order.getTotalWeight().subtract(order.getTotalWeightFulfilled());
        if (request.getAllocatedWeight().compareTo(remaining) > 0) {
            throw new FerosException(
                    "Allocated weight (" + request.getAllocatedWeight() +
                    "T) exceeds remaining weight (" + remaining + "T)", HttpStatus.BAD_REQUEST);
        }

        OrderVehicleAllocation allocation = OrderVehicleAllocation.builder()
                .tenant(tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow())
                .order(order)
                .vehicle(vehicle)
                .allocatedWeight(request.getAllocatedWeight())
                .expectedLoadDate(request.getExpectedLoadDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .allocationStatus(VehicleAllocationStatus.ALLOCATED)
                .remarks(request.getRemarks())
                .allocatedBy(getCurrentUser())
                .isActive(true)
                .build();

        vehicleAllocationRepository.save(allocation);

        // Update order weight fulfilled and status
        BigDecimal newFulfilled = order.getTotalWeightFulfilled()
                .add(request.getAllocatedWeight());
        order.setTotalWeightFulfilled(newFulfilled);

        if (newFulfilled.compareTo(order.getTotalWeight()) >= 0) {
            order.setOrderStatus(OrderStatus.FULLY_ASSIGNED);
        } else {
            order.setOrderStatus(OrderStatus.PARTIALLY_ASSIGNED);
        }
        orderRepository.save(order);

        return mapToVehicleAllocationResponse(allocation);
    }

    @Override
    @Transactional
    public StaffAllocationResponse assignStaff(Long orderId, AssignStaffRequest request) {
        Long tenantId = getCurrentTenantId();

        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        OrderVehicleAllocation vehicleAllocation = vehicleAllocationRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getVehicleAllocationId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle allocation not found", HttpStatus.NOT_FOUND));

        if (staffAllocationRepository.existsByVehicleAllocationIdAndUserIdAndIsActiveTrue(
                request.getVehicleAllocationId(), request.getUserId())) {
            throw new FerosException("Staff already assigned to this vehicle allocation",
                    HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        // Get user's role
        Role role = user.getRoles().stream().findFirst()
                .orElseThrow(() -> new FerosException("User has no role", HttpStatus.BAD_REQUEST));

        OrderStaffAllocation staffAllocation = OrderStaffAllocation.builder()
                .tenant(tenantRepository.findByIdAndIsActiveTrue(tenantId).orElseThrow())
                .order(order)
                .vehicleAllocation(vehicleAllocation)
                .user(user)
                .role(role)
                .expectedStartDate(request.getExpectedStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .remarks(request.getRemarks())
                .allocatedBy(getCurrentUser())
                .isActive(true)
                .build();

        return mapToStaffAllocationResponse(staffAllocationRepository.save(staffAllocation));
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long id, OrderPaymentStatus paymentStatus) {
        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new FerosException("Cannot update payment status of a cancelled order", HttpStatus.BAD_REQUEST);
        }

        order.setOrderPaymentStatus(paymentStatus);

        if (paymentStatus == OrderPaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        }

        return mapToOrderResponse(orderRepository.save(order));
    }

    // ===================== MAPPERS =====================
    private OrderResponse mapToOrderResponse(Order o) {
        List<VehicleAllocationResponse> vehicleAllocations = vehicleAllocationRepository
                .findByOrderIdAndIsActiveTrue(o.getId())
                .stream().map(this::mapToVehicleAllocationResponse).toList();

        return OrderResponse.builder()
                .id(o.getId())
                .tenantId(o.getTenant().getId())
                .orderNumber(o.getOrderNumber())
                .orderDate(o.getOrderDate())
                .expectedDeliveryDate(o.getExpectedDeliveryDate())
                .clientId(o.getClient().getId())
                .clientName(o.getClient().getClientName())
                .materialTypeId(o.getMaterialType().getId())
                .materialTypeName(o.getMaterialType().getName())
                .totalWeight(o.getTotalWeight())
                .totalWeightFulfilled(o.getTotalWeightFulfilled())
                .remainingWeight(o.getTotalWeight().subtract(o.getTotalWeightFulfilled()))
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
                .totalFreightAmount(o.getTotalFreightAmount())
                .orderStatus(o.getOrderStatus())
                .orderPaymentStatus(o.getOrderPaymentStatus())
                .specialInstructions(o.getSpecialInstructions())
                .remarks(o.getRemarks())
                .createdById(o.getCreatedBy().getId())
                .createdByName(o.getCreatedBy().getName())
                .vehicleAllocations(vehicleAllocations)
                .isActive(o.getIsActive())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private VehicleAllocationResponse mapToVehicleAllocationResponse(OrderVehicleAllocation a) {
        List<StaffAllocationResponse> staffAllocations = staffAllocationRepository
                .findByVehicleAllocationIdAndIsActiveTrue(a.getId())
                .stream().map(this::mapToStaffAllocationResponse).toList();

        return VehicleAllocationResponse.builder()
                .id(a.getId())
                .vehicleId(a.getVehicle().getId())
                .vehicleRegistrationNumber(a.getVehicle().getRegistrationNumber())
                .vehicleTypeName(a.getVehicle().getVehicleType() != null ?
                        a.getVehicle().getVehicleType().getName() : null)
                .allocatedWeight(a.getAllocatedWeight())
                .expectedLoadDate(a.getExpectedLoadDate())
                .expectedDeliveryDate(a.getExpectedDeliveryDate())
                .actualLoadDate(a.getActualLoadDate())
                .actualDeliveryDate(a.getActualDeliveryDate())
                .allocationStatus(a.getAllocationStatus())
                .remarks(a.getRemarks())
                .staffAllocations(staffAllocations)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private StaffAllocationResponse mapToStaffAllocationResponse(OrderStaffAllocation s) {
        return StaffAllocationResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .userName(s.getUser().getName())
                .roleName(s.getRole().getName().name())
                .expectedStartDate(s.getExpectedStartDate())
                .expectedEndDate(s.getExpectedEndDate())
                .actualStartDate(s.getActualStartDate())
                .actualEndDate(s.getActualEndDate())
                .allocationStatus(s.getAllocationStatus())
                .remarks(s.getRemarks())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}