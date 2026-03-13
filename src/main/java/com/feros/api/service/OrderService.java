package com.feros.api.service;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrder(Long id, OrderRequest request);
    void cancelOrder(Long id);
    VehicleAllocationResponse assignVehicle(Long orderId, AssignVehicleRequest request);
    StaffAllocationResponse assignStaff(Long orderId, AssignStaffRequest request);
}