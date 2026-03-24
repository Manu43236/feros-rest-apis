package com.feros.api.repository;

import com.feros.api.entity.OrderVehicleAllocation;
import com.feros.api.enums.VehicleAllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT ova FROM OrderVehicleAllocation ova WHERE ova.tenant.id = :tenantId AND ova.isActive = true " +
           "AND ova.allocationStatus IN ('ALLOCATED', 'LR_CREATED', 'IN_TRANSIT') " +
           "AND (ova.expectedLoadDate IS NULL OR ova.expectedLoadDate <= :date) " +
           "AND (ova.expectedDeliveryDate IS NULL OR ova.expectedDeliveryDate >= :date)")
    List<OrderVehicleAllocation> findActiveAllocationsOnDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(ova) > 0 THEN true ELSE false END FROM OrderVehicleAllocation ova " +
           "WHERE ova.vehicle.id = :vehicleId AND ova.isActive = true " +
           "AND ova.allocationStatus IN ('ALLOCATED', 'LR_CREATED', 'IN_TRANSIT') " +
           "AND ova.expectedLoadDate <= :deliveryDate AND ova.expectedDeliveryDate >= :loadDate")
    boolean existsVehicleConflict(@Param("vehicleId") Long vehicleId,
                                   @Param("loadDate") LocalDate loadDate,
                                   @Param("deliveryDate") LocalDate deliveryDate);
}