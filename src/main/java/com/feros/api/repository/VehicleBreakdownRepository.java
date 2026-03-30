package com.feros.api.repository;

import com.feros.api.entity.VehicleBreakdown;
import com.feros.api.enums.BreakdownStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBreakdownRepository extends JpaRepository<VehicleBreakdown, Long> {

    Optional<VehicleBreakdown> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);

    boolean existsByVehicleAllocationIdAndIsActiveTrueAndStatusNotIn(
            Long vehicleAllocationId, List<BreakdownStatus> excludedStatuses);

    List<VehicleBreakdown> findByVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(Long vehicleId);

    Optional<VehicleBreakdown> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    List<VehicleBreakdown> findByOrderIdAndIsActiveTrue(Long orderId);
}
