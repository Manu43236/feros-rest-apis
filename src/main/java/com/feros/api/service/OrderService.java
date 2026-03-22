package com.feros.api.service;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.enums.OrderPaymentStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrder(Long id, OrderRequest request);
    void cancelOrder(Long id);
    VehicleAllocationResponse assignVehicle(Long orderId, AssignVehicleRequest request);
    void unassignVehicle(Long orderId, Long allocationId);
    StaffAllocationResponse assignStaff(Long orderId, AssignStaffRequest request);
    OrderResponse updatePaymentStatus(Long id, OrderPaymentStatus paymentStatus);
}