package com.feros.api.repository;

import com.feros.api.entity.VehicleTireFitting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleTireFittingRepository extends JpaRepository<VehicleTireFitting, Long> {

    // Currently fitted (not yet removed)
    List<VehicleTireFitting> findByVehicleIdAndRemovedAtKmIsNullAndIsActiveTrueOrderByPositionDisplayOrderAsc(Long vehicleId);

    // Currently fitted tire at a specific position
    Optional<VehicleTireFitting> findByPositionIdAndRemovedAtKmIsNullAndIsActiveTrue(Long positionId);

    // History for a vehicle
    List<VehicleTireFitting> findByVehicleIdAndIsActiveTrueOrderByFittedDateDescIdDesc(Long vehicleId);

    // History for a specific tire
    List<VehicleTireFitting> findByTireIdAndIsActiveTrueOrderByFittedDateDescIdDesc(Long tireId);

    // Check if a tire is currently fitted anywhere
    @Query("SELECT f FROM VehicleTireFitting f WHERE f.tire.id = :tireId AND f.removedAtKm IS NULL AND f.isActive = true")
    Optional<VehicleTireFitting> findCurrentFittingForTire(@Param("tireId") Long tireId);

    // All active fittings for a tenant — used to build vehicle context on tire list
    @Query("SELECT f FROM VehicleTireFitting f WHERE f.tenant.id = :tenantId AND f.removedAtKm IS NULL AND f.isActive = true")
    List<VehicleTireFitting> findAllActiveFittingsByTenantId(@Param("tenantId") Long tenantId);
}
