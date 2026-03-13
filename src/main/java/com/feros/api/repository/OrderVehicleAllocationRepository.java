package com.feros.api.repository;

import com.feros.api.entity.OrderVehicleAllocation;
import com.feros.api.enums.VehicleAllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderVehicleAllocationRepository extends JpaRepository<OrderVehicleAllocation, Long> {
    List<OrderVehicleAllocation> findByOrderIdAndIsActiveTrue(Long orderId);
    Optional<OrderVehicleAllocation> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByOrderIdAndVehicleIdAndIsActiveTrue(Long orderId, Long vehicleId);
    long countByTenantIdAndAllocationStatusAndIsActiveTrue(Long tenantId, VehicleAllocationStatus status);
    @Query("SELECT COUNT(DISTINCT ova.vehicle.id) FROM OrderVehicleAllocation ova WHERE ova.tenant.id = :tenantId AND ova.allocationStatus = :status AND ova.isActive = true")
    long countDistinctVehiclesByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") VehicleAllocationStatus status);
}