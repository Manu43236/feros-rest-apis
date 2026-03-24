package com.feros.api.repository;

import com.feros.api.entity.OrderStaffAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStaffAllocationRepository extends JpaRepository<OrderStaffAllocation, Long> {
    List<OrderStaffAllocation> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);
    boolean existsByVehicleAllocationIdAndUserIdAndIsActiveTrue(Long vehicleAllocationId, Long userId);
    long countByUserIdAndAllocationStatusAndIsActiveTrue(Long userId, com.feros.api.enums.StaffAllocationStatus status);
    Optional<OrderStaffAllocation> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    // Fix: NULL dates treated as open-ended (no boundary) to prevent silent double-booking
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN ('ALLOCATED', 'IN_TRANSIT') " +
           "AND (sa.expectedStartDate IS NULL OR sa.expectedStartDate <= :endDate) " +
           "AND (sa.expectedEndDate IS NULL OR sa.expectedEndDate >= :startDate)")
    boolean existsStaffConflict(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // Within-order conflict: same user already assigned to a different vehicle in this order
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN ('ALLOCATED', 'IN_TRANSIT') " +
           "AND sa.order.id = :orderId " +
           "AND sa.vehicleAllocation.id <> :excludeVehicleAllocationId")
    boolean existsWithinOrderConflict(@Param("userId") Long userId,
                                       @Param("orderId") Long orderId,
                                       @Param("excludeVehicleAllocationId") Long excludeVehicleAllocationId);

    // Active allocation for a user (ALLOCATED or IN_TRANSIT) — used for isAssigned + guards
    @Query("SELECT sa FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN ('ALLOCATED', 'IN_TRANSIT') " +
           "ORDER BY sa.createdAt DESC")
    List<OrderStaffAllocation> findActiveAllocationsForUser(@Param("userId") Long userId);
}