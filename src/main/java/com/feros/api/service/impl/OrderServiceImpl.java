package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.City;
import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.State;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.LrStatus;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleAllocationStatus;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.repository.LrRepository;
import com.feros.api.service.NotificationService;
import com.feros.api.service.OrderService;
import com.feros.api.enums.NotificationType;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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
    private final VehicleStatusRepository vehicleStatusRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LrRepository lrRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

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

    private void setVehicleStatus(Vehicle vehicle, VehicleStatusType type) {
        vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(type)
                .ifPresent(status -> {
                    vehicle.setCurrentStatus(status);
                    vehicleRepository.save(vehicle);
                });
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
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : TimeUtil.today())
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

        Order savedOrder = orderRepository.save(order);
        notificationService.sendToRoles(savedOrder.getTenant(), List.of(RoleName.SUPERVISOR, RoleName.ADMIN),
                NotificationType.ORDER_CREATED,
                "New Order Created",
                "Order " + savedOrder.getOrderNumber() + " from " + savedOrder.getSourceCity().getName() + " to " + savedOrder.getDestinationCity().getName() + " is ready for vehicle assignment.");
        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return mapToOrderResponse(orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public Page<OrderResponse> getAllOrders(int page, int size, String search, OrderStatus status) {
        Long tenantId = getCurrentTenantId();
        String role = SecurityUtil.getCurrentRole();

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Specification<Order> spec = buildOrderSpec(tenantId, role, search, status);
        return orderRepository.findAll(spec, pageable).map(this::mapToOrderResponse);
    }

    private Specification<Order> buildOrderSpec(Long tenantId, String role, String search, OrderStatus status) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.isTrue(root.get("isActive")));

            // DRIVER / CLEANER only see orders they are staff-allocated on
            if (("DRIVER".equals(role) || "CLEANER".equals(role)) && query != null) {
                Long userId = SecurityUtil.getCurrentUserId();
                var sub = query.subquery(Long.class);
                var saRoot = sub.from(com.feros.api.entity.OrderStaffAllocation.class);
                sub.select(saRoot.get("order").get("id"))
                   .where(cb.equal(saRoot.get("user").get("id"), userId),
                          cb.isTrue(saRoot.get("isActive")));
                predicates.add(root.get("id").in(sub));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("orderStatus"), status));
            }

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(root.join("client", jakarta.persistence.criteria.JoinType.LEFT).get("clientName")), like),
                    cb.like(cb.lower(root.join("sourceCity", jakarta.persistence.criteria.JoinType.LEFT).get("name")), like),
                    cb.like(cb.lower(root.join("destinationCity", jakarta.persistence.criteria.JoinType.LEFT).get("name")), like)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        String role = SecurityUtil.getCurrentRole();
        if ("DRIVER".equals(role) || "CLEANER".equals(role)) {
            if (status != OrderStatus.IN_TRANSIT && status != OrderStatus.DELIVERED) {
                throw new FerosException("Drivers can only update status to IN_TRANSIT or DELIVERED",
                        HttpStatus.FORBIDDEN);
            }
        }

        if (status == OrderStatus.CANCELLED) {
            List<com.feros.api.entity.Lr> activeLrs = lrRepository.findByOrderIdAndIsActiveTrue(id);
            boolean hasNonCancellableLr = activeLrs.stream().anyMatch(lr ->
                    lr.getLrStatus() == com.feros.api.enums.LrStatus.DELIVERED
                    || lr.getLrStatus() == com.feros.api.enums.LrStatus.IN_TRANSIT
                    || lr.getLrStatus() == com.feros.api.enums.LrStatus.WEIGHT_LOADED);
            if (hasNonCancellableLr) {
                throw new FerosException(
                        "Cannot cancel order — one or more LRs are already delivered or in transit",
                        HttpStatus.BAD_REQUEST);
            }
        }

        order.setOrderStatus(status);
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse forceDeliverOrder(Long id) {
        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        // Guard: all existing (non-cancelled) LRs must be DELIVERED
        List<com.feros.api.entity.Lr> activeLrs = lrRepository.findByOrderIdAndIsActiveTrue(id);
        boolean hasUndeliveredLr = activeLrs.stream()
                .anyMatch(lr -> lr.getLrStatus() != com.feros.api.enums.LrStatus.CANCELLED
                             && lr.getLrStatus() != com.feros.api.enums.LrStatus.DELIVERED);
        if (hasUndeliveredLr) {
            throw new FerosException(
                "Cannot mark order as delivered — one or more LRs are not yet delivered",
                HttpStatus.BAD_REQUEST);
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        return mapToOrderResponse(orderRepository.save(order));
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

        // Cancel and release all vehicle allocations
        List<OrderVehicleAllocation> vehicleAllocations =
                vehicleAllocationRepository.findByOrderIdAndIsActiveTrue(order.getId());
        for (OrderVehicleAllocation va : vehicleAllocations) {
            // Cancel all staff allocations linked to this vehicle allocation
            List<OrderStaffAllocation> staffAllocations =
                    staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(va.getId());
            for (OrderStaffAllocation sa : staffAllocations) {
                sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
                sa.setIsActive(false);
            }
            staffAllocationRepository.saveAll(staffAllocations);
            for (OrderStaffAllocation sa : staffAllocations) {
                notificationService.sendToUser(order.getTenant(), sa.getUser(), NotificationType.TRIP_UNASSIGNED,
                        "Order Cancelled",
                        "Order " + order.getOrderNumber() + " has been cancelled. Your trip assignment has been removed.");
            }

            va.setAllocationStatus(VehicleAllocationStatus.CANCELLED);
            va.setIsActive(false);

            // Restore vehicle status → AVAILABLE
            setVehicleStatus(va.getVehicle(), VehicleStatusType.AVAILABLE);
        }
        vehicleAllocationRepository.saveAll(vehicleAllocations);

        // Cancel all LRs linked to this order
        List<com.feros.api.entity.Lr> lrs = lrRepository.findByOrderIdAndIsActiveTrue(order.getId());
        for (com.feros.api.entity.Lr lr : lrs) {
            lr.setLrStatus(com.feros.api.enums.LrStatus.CANCELLED);
            lr.setIsActive(false);
        }
        lrRepository.saveAll(lrs);

        order.setOrderStatus(OrderStatus.CANCELLED);
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

        if (request.getExpectedLoadDate() != null && request.getExpectedDeliveryDate() != null &&
                vehicleAllocationRepository.existsVehicleConflict(
                        request.getVehicleId(), request.getExpectedLoadDate(), request.getExpectedDeliveryDate())) {
            throw new FerosException(
                    "Vehicle is already assigned to another order during this date range",
                    HttpStatus.CONFLICT);
        }

        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        if (vehicle.getCurrentStatus() != null &&
                (vehicle.getCurrentStatus().getStatusType() == VehicleStatusType.IN_REPAIR ||
                 vehicle.getCurrentStatus().getStatusType() == VehicleStatusType.BREAKDOWN)) {
            throw new FerosException(
                "Cannot assign vehicle — it is currently marked as " +
                vehicle.getCurrentStatus().getName() + ". Resolve the issue first.",
                HttpStatus.CONFLICT);
        }

        // Validate allocated weight doesn't exceed 1.5x vehicle capacity
        if (vehicle.getCapacityInTons() != null) {
            BigDecimal maxAllowed = vehicle.getCapacityInTons()
                    .multiply(new BigDecimal("1.5"));
            if (request.getAllocatedWeight().compareTo(maxAllowed) > 0) {
                throw new FerosException(
                        "Allocated weight (" + request.getAllocatedWeight() +
                        "T) exceeds 1.5x vehicle capacity (" + vehicle.getCapacityInTons() +
                        "T × 1.5 = " + maxAllowed + "T)", HttpStatus.BAD_REQUEST);
            }
        }

        // Validate allocated weight doesn't exceed remaining order weight
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

        // Update vehicle status → ASSIGNED
        setVehicleStatus(vehicle, VehicleStatusType.ASSIGNED);

        // Update order weight fulfilled and status
        BigDecimal newFulfilled = order.getTotalWeightFulfilled()
                .add(request.getAllocatedWeight());
        order.setTotalWeightFulfilled(newFulfilled);

        if (newFulfilled.compareTo(order.getTotalWeight()) >= 0) {
            order.setOrderStatus(OrderStatus.FULLY_ASSIGNED);
        } else {
            // Don't downgrade if order already has deliveries in progress
            boolean hasActiveDelivery = order.getOrderStatus() == OrderStatus.PARTIALLY_DELIVERED
                    || order.getOrderStatus() == OrderStatus.IN_TRANSIT;
            if (!hasActiveDelivery) {
                order.setOrderStatus(OrderStatus.PARTIALLY_ASSIGNED);
            }
        }
        orderRepository.save(order);

        return mapToVehicleAllocationResponse(allocation);
    }

    @Override
    @Transactional
    public void unassignVehicle(Long orderId, Long allocationId) {
        Long tenantId = getCurrentTenantId();

        Order order = orderRepository
                .findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        OrderVehicleAllocation allocation = vehicleAllocationRepository
                .findByIdAndTenantIdAndIsActiveTrue(allocationId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle allocation not found", HttpStatus.NOT_FOUND));

        if (!allocation.getOrder().getId().equals(orderId)) {
            throw new FerosException("Allocation does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        if (allocation.getAllocationStatus() != VehicleAllocationStatus.ALLOCATED) {
            throw new FerosException(
                    "Cannot unassign vehicle — allocation is already " + allocation.getAllocationStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        if (lrRepository.existsByVehicleAllocationIdAndLrStatusNot(allocationId, LrStatus.CANCELLED)) {
            throw new FerosException(
                    "Cannot unassign vehicle — an LR has already been created for this allocation",
                    HttpStatus.CONFLICT);
        }

        // Guard: block unassign if any staff is IN_TRANSIT on this vehicle
        List<OrderStaffAllocation> linkedStaff =
                staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(allocation.getId());
        boolean anyInTransit = linkedStaff.stream()
                .anyMatch(sa -> sa.getAllocationStatus() == StaffAllocationStatus.IN_TRANSIT);
        if (anyInTransit) {
            throw new FerosException(
                    "Cannot unassign vehicle — staff is currently in transit on this vehicle",
                    HttpStatus.CONFLICT);
        }

        // Cancel all staff allocations linked to this vehicle allocation
        for (OrderStaffAllocation sa : linkedStaff) {
            sa.setAllocationStatus(StaffAllocationStatus.CANCELLED);
            sa.setIsActive(false);
        }
        staffAllocationRepository.saveAll(linkedStaff);
        String unassignedVehicleReg = allocation.getVehicle().getRegistrationNumber();
        for (OrderStaffAllocation sa : linkedStaff) {
            notificationService.sendToUser(order.getTenant(), sa.getUser(), NotificationType.TRIP_UNASSIGNED,
                    "Vehicle Unassigned",
                    "Vehicle " + unassignedVehicleReg + " has been removed from Order " + order.getOrderNumber() + ". Your trip assignment has been removed.");
        }

        // Deduct the allocated weight from order
        BigDecimal newFulfilled = order.getTotalWeightFulfilled()
                .subtract(allocation.getAllocatedWeight());
        if (newFulfilled.compareTo(BigDecimal.ZERO) < 0) newFulfilled = BigDecimal.ZERO;
        order.setTotalWeightFulfilled(newFulfilled);

        // Recalculate order status
        if (newFulfilled.compareTo(BigDecimal.ZERO) == 0) {
            order.setOrderStatus(OrderStatus.PENDING);
        } else if (newFulfilled.compareTo(order.getTotalWeight()) >= 0) {
            order.setOrderStatus(OrderStatus.FULLY_ASSIGNED);
        } else {
            order.setOrderStatus(OrderStatus.PARTIALLY_ASSIGNED);
        }
        orderRepository.save(order);

        // Cancel and soft-delete vehicle allocation
        allocation.setAllocationStatus(VehicleAllocationStatus.CANCELLED);
        allocation.setIsActive(false);
        vehicleAllocationRepository.save(allocation);

        // Restore vehicle status → AVAILABLE
        setVehicleStatus(allocation.getVehicle(), VehicleStatusType.AVAILABLE);
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

        // Within-order conflict: same staff on a different vehicle in this order
        List<StaffAllocationStatus> activeStatuses = List.of(
                StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);

        if (staffAllocationRepository.existsWithinOrderConflict(
                request.getUserId(), orderId, request.getVehicleAllocationId(), activeStatuses)) {
            throw new FerosException(
                    "Staff is already assigned to another vehicle in this order",
                    HttpStatus.CONFLICT);
        }

        if (request.getExpectedStartDate() != null && request.getExpectedEndDate() != null
                && staffAllocationRepository.existsStaffConflict(
                        request.getUserId(), request.getExpectedStartDate(),
                        request.getExpectedEndDate(), activeStatuses)) {
            throw new FerosException(
                    "Staff is already assigned to another order during this date range",
                    HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        // Get user's role
        Role role = user.getRoles().stream().findFirst()
                .orElseThrow(() -> new FerosException("User has no role", HttpStatus.BAD_REQUEST));

        // Validate role matches the slot (DRIVER slot → must be DRIVER, CLEANER slot → must be CLEANER)
        if (request.getSlotRole() != null) {
            String userRoleName = role.getName().name();
            if (!userRoleName.equals(request.getSlotRole())) {
                String expected = request.getSlotRole().equals("DRIVER") ? "a Driver" : "a Cleaner";
                throw new FerosException("Selected user is not " + expected + ". Only " + request.getSlotRole().toLowerCase() + "s can be assigned to this slot.", HttpStatus.BAD_REQUEST);
            }
        }

        // Validate staff has marked attendance today (PENDING or APPROVED)
        List<AttendanceApprovalStatus> validAttendanceStatuses = List.of(
                AttendanceApprovalStatus.PENDING, AttendanceApprovalStatus.APPROVED);
        boolean hasAttendanceToday = attendanceRepository
                .existsByUserIdAndTenantIdAndAttendanceDateAndApprovalStatusInAndIsActiveTrue(
                        request.getUserId(), tenantId, TimeUtil.today(), validAttendanceStatuses);
        if (!hasAttendanceToday) {
            throw new FerosException(
                    "This " + role.getName().name().toLowerCase() + " has not marked attendance today and cannot be assigned.",
                    HttpStatus.BAD_REQUEST);
        }

        // Validate user is not currently on an active trip
        boolean currentlyAssigned = !staffAllocationRepository.findActiveAllocationsForUser(request.getUserId(), activeStatuses).isEmpty();
        if (currentlyAssigned)
            throw new FerosException("This " + role.getName().name().toLowerCase() + " is currently assigned to another order and is not available.", HttpStatus.CONFLICT);

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

        OrderStaffAllocation saved = staffAllocationRepository.save(staffAllocation);

        // Sync vehicle-level staff assignment
        Vehicle vehicle = vehicleAllocation.getVehicle();
        RoleName roleName = role.getName();
        if (roleName == RoleName.DRIVER) {
            vehicle.setCurrentDriver(user);
            vehicleRepository.save(vehicle);
        } else if (roleName == RoleName.CLEANER) {
            vehicle.setCurrentCleaner(user);
            vehicleRepository.save(vehicle);
        }

        String vehicleReg = vehicleAllocation.getVehicle().getRegistrationNumber();
        String destination = order.getDestinationCity().getName();
        notificationService.sendToUser(saved.getTenant(), user, NotificationType.TRIP_ASSIGNED,
                "Trip Assigned",
                "You have been assigned to vehicle " + vehicleReg + " for trip to " + destination + " (Order: " + order.getOrderNumber() + ")");

        return mapToStaffAllocationResponse(saved);
    }

    @Override
    @Transactional
    public void unassignStaff(Long orderId, Long staffAllocationId) {
        Long tenantId = getCurrentTenantId();

        orderRepository.findByIdAndTenantIdAndIsActiveTrue(orderId, tenantId)
                .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));

        OrderStaffAllocation allocation = staffAllocationRepository
                .findByIdAndTenantIdAndIsActiveTrue(staffAllocationId, tenantId)
                .orElseThrow(() -> new FerosException("Staff allocation not found", HttpStatus.NOT_FOUND));

        if (!allocation.getOrder().getId().equals(orderId)) {
            throw new FerosException("Allocation does not belong to this order", HttpStatus.BAD_REQUEST);
        }

        if (allocation.getAllocationStatus() == StaffAllocationStatus.IN_TRANSIT) {
            throw new FerosException(
                    "Cannot unassign staff — they are currently in transit",
                    HttpStatus.CONFLICT);
        }

        if (allocation.getAllocationStatus() == StaffAllocationStatus.COMPLETED) {
            throw new FerosException(
                    "Cannot unassign staff — allocation is already completed",
                    HttpStatus.BAD_REQUEST);
        }

        allocation.setAllocationStatus(StaffAllocationStatus.CANCELLED);
        allocation.setIsActive(false);
        staffAllocationRepository.save(allocation);

        String vehicleReg = allocation.getVehicleAllocation().getVehicle().getRegistrationNumber();
        notificationService.sendToUser(allocation.getTenant(), allocation.getUser(), NotificationType.TRIP_UNASSIGNED,
                "Trip Unassigned",
                "You have been removed from vehicle " + vehicleReg + " (Order: " + allocation.getOrder().getOrderNumber() + ")");
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

        if (paymentStatus == OrderPaymentStatus.PAID || paymentStatus == OrderPaymentStatus.PARTIALLY_PAID) {
            boolean hasDelivery = order.getOrderStatus() == OrderStatus.PARTIALLY_DELIVERED
                    || order.getOrderStatus() == OrderStatus.DELIVERED
                    || order.getOrderStatus() == OrderStatus.COMPLETED;
            if (!hasDelivery) {
                throw new FerosException(
                        "Payment cannot be marked as " + paymentStatus.name().replace("_", " ") +
                        " — order must be at least partially delivered first",
                        HttpStatus.BAD_REQUEST);
            }
        }

        order.setOrderPaymentStatus(paymentStatus);

        if (paymentStatus == OrderPaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.COMPLETED);

            // Release any remaining active vehicle and staff allocations
            List<OrderVehicleAllocation> vehicleAllocations =
                    vehicleAllocationRepository.findByOrderIdAndIsActiveTrue(order.getId());
            for (OrderVehicleAllocation va : vehicleAllocations) {
                if (va.getAllocationStatus() != VehicleAllocationStatus.CANCELLED) {
                    va.setAllocationStatus(VehicleAllocationStatus.DELIVERED);
                    if (va.getActualDeliveryDate() == null) {
                        va.setActualDeliveryDate(TimeUtil.today());
                    }
                    List<OrderStaffAllocation> staffAllocations =
                            staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(va.getId());
                    for (OrderStaffAllocation sa : staffAllocations) {
                        if (sa.getAllocationStatus() != StaffAllocationStatus.CANCELLED) {
                            sa.setAllocationStatus(StaffAllocationStatus.COMPLETED);
                            if (sa.getActualEndDate() == null) {
                                sa.setActualEndDate(TimeUtil.today());
                            }
                        }
                    }
                    staffAllocationRepository.saveAll(staffAllocations);
                }
            }
            for (OrderVehicleAllocation va : vehicleAllocations) {
                if (va.getAllocationStatus() != VehicleAllocationStatus.CANCELLED) {
                    setVehicleStatus(va.getVehicle(), VehicleStatusType.AVAILABLE);
                }
            }
            vehicleAllocationRepository.saveAll(vehicleAllocations);
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