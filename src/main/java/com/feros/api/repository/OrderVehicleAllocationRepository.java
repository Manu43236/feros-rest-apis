package com.feros.api.repository;

import com.feros.api.entity.OrderVehicleAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderVehicleAllocationRepository extends JpaRepository<OrderVehicleAllocation, Long> {
    List<OrderVehicleAllocation> findByOrderIdAndIsActiveTrue(Long orderId);
    Optional<OrderVehicleAllocation> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByOrderIdAndVehicleIdAndIsActiveTrue(Long orderId, Long vehicleId);
}