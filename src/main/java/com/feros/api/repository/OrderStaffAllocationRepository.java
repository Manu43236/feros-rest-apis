package com.feros.api.repository;

import com.feros.api.entity.OrderStaffAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderStaffAllocationRepository extends JpaRepository<OrderStaffAllocation, Long> {
    List<OrderStaffAllocation> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);
    boolean existsByVehicleAllocationIdAndUserIdAndIsActiveTrue(Long vehicleAllocationId, Long userId);
    long countByUserIdAndAllocationStatusAndIsActiveTrue(Long userId, com.feros.api.enums.StaffAllocationStatus status);

    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN ('ALLOCATED', 'IN_TRANSIT') " +
           "AND sa.expectedStartDate <= :endDate AND sa.expectedEndDate >= :startDate")
    boolean existsStaffConflict(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}