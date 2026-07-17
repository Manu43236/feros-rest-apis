package com.feros.api.controller;

import com.feros.api.dto.request.AssignStaffRequest;
import com.feros.api.dto.request.AssignVehicleRequest;
import com.feros.api.dto.request.OrderRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.StaffAllocationResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.feros.api.dto.response.VehicleAssignmentHistoryResponse;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;

import java.util.List;
import com.feros.api.service.OrderService;
import com.feros.api.service.impl.OrderPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService    orderService;
    private final OrderPdfService orderPdfService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched successfully", orderService.getAllOrders(page, size, search, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
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

    @DeleteMapping("/{id}/allocations/{allocationId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> unassignVehicle(
            @PathVariable Long id, @PathVariable Long allocationId) {
        orderService.unassignVehicle(id, allocationId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle unassigned successfully", null));
    }

    @PostMapping("/{id}/assign-staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<StaffAllocationResponse>> assignStaff(
            @PathVariable Long id, @Valid @RequestBody AssignStaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff assigned successfully", orderService.assignStaff(id, request)));
    }

    @DeleteMapping("/{id}/staff-allocations/{staffAllocationId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> unassignStaff(
            @PathVariable Long id, @PathVariable Long staffAllocationId) {
        orderService.unassignStaff(id, staffAllocationId);
        return ResponseEntity.ok(ApiResponse.success("Staff unassigned successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR', 'DRIVER', 'CLEANER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated successfully", orderService.updateOrderStatus(id, status)));
    }

    @PatchMapping("/{id}/force-deliver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<OrderResponse>> forceDeliverOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order marked as delivered", orderService.forceDeliverOrder(id)));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<byte[]> getOrderPdf(@PathVariable Long id,
            @RequestParam(defaultValue = "FEROS") String companyName) {
        OrderResponse order = orderService.getOrderById(id);
        byte[] pdf = orderPdfService.generatePdf(order, companyName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"ORDER-" + order.getOrderNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/vehicle-allocation-history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentHistoryResponse>>> getAllVehicleAllocationHistory() {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle allocation history fetched", orderService.getAllVehicleAllocationHistory()));
    }

    @GetMapping("/vehicle-allocation-history/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentHistoryResponse>>> getVehicleAllocationHistoryByVehicle(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle allocation history fetched", orderService.getVehicleAllocationHistory(vehicleId)));
    }
}