package com.feros.api.repository;

import com.feros.api.entity.OrderStaffAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStaffAllocationRepository extends JpaRepository<OrderStaffAllocation, Long> {
    List<OrderStaffAllocation> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);
    boolean existsByVehicleAllocationIdAndUserIdAndIsActiveTrue(Long vehicleAllocationId, Long userId);
}