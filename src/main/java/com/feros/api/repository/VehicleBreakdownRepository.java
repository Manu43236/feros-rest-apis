package com.feros.api.repository;

import com.feros.api.entity.VehicleBreakdown;
import com.feros.api.enums.BreakdownStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBreakdownRepository extends JpaRepository<VehicleBreakdown, Long> {

    Optional<VehicleBreakdown> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);

    boolean existsByVehicleAllocationIdAndIsActiveTrueAndStatusNotIn(
            Long vehicleAllocationId, List<BreakdownStatus> excludedStatuses);

    List<VehicleBreakdown> findByVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(Long vehicleId);

    List<VehicleBreakdown> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    Optional<VehicleBreakdown> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    List<VehicleBreakdown> findByOrderIdAndIsActiveTrue(Long orderId);

    @Query("SELECT b FROM VehicleBreakdown b WHERE b.tenant.id = :tenantId AND b.isActive = true " +
           "AND FUNCTION('DATE', b.breakdownDate) BETWEEN :from AND :to ORDER BY b.breakdownDate DESC")
    List<VehicleBreakdown> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);
}
