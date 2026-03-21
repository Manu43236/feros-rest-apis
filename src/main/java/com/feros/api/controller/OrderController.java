package com.feros.api.controller;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched successfully", orderService.getAllOrders()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched successfully", orderService.getOrderById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order created successfully", orderService.createOrder(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order updated successfully", orderService.updateOrder(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
    }

    @PostMapping("/{id}/assign-vehicle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<VehicleAllocationResponse>> assignVehicle(
            @PathVariable Long id, @Valid @RequestBody AssignVehicleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle assigned successfully", orderService.assignVehicle(id, request)));
    }

    @PatchMapping("/{id}/payment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id, @RequestParam OrderPaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment status updated successfully", orderService.updatePaymentStatus(id, status)));
    }

    @PostMapping("/{id}/assign-staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<StaffAllocationResponse>> assignStaff(
            @PathVariable Long id, @Valid @RequestBody AssignStaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff assigned successfully", orderService.assignStaff(id, request)));
    }
}