package com.feros.api.service;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getAllOrders(int page, int size, String search, OrderStatus status);
    OrderResponse updateOrder(Long id, OrderRequest request);
    void cancelOrder(Long id);
    VehicleAllocationResponse assignVehicle(Long orderId, AssignVehicleRequest request);
    void unassignVehicle(Long orderId, Long allocationId);
    StaffAllocationResponse assignStaff(Long orderId, AssignStaffRequest request);
    void unassignStaff(Long orderId, Long staffAllocationId);
    OrderResponse updateOrderStatus(Long id, OrderStatus status);
    OrderResponse forceDeliverOrder(Long id);
    OrderResponse updatePaymentStatus(Long id, OrderPaymentStatus paymentStatus);
}