package com.feros.api.repository;

import com.feros.api.entity.VehicleTyreFitting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleTyreFittingRepository extends JpaRepository<VehicleTyreFitting, Long> {

    // Currently fitted (not yet removed) — use removedDate as the canonical removed flag
    List<VehicleTyreFitting> findByVehicleIdAndRemovedDateIsNullAndIsActiveTrueOrderByPositionDisplayOrderAsc(Long vehicleId);

    // Currently fitted tyre at a specific position
    Optional<VehicleTyreFitting> findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(Long positionId);

    // History for a vehicle
    List<VehicleTyreFitting> findByVehicleIdAndIsActiveTrueOrderByFittedDateDescIdDesc(Long vehicleId);

    // History for a specific tyre
    List<VehicleTyreFitting> findByTyreIdAndIsActiveTrueOrderByFittedDateDescIdDesc(Long tyreId);

    // Check if a tyre is currently fitted anywhere
    @Query("SELECT f FROM VehicleTyreFitting f WHERE f.tyre.id = :tyreId AND f.removedDate IS NULL AND f.isActive = true")
    Optional<VehicleTyreFitting> findCurrentFittingForTyre(@Param("tyreId") Long tyreId);

    // All active fittings for a tenant — used to build vehicle context on tyre list
    @Query("SELECT f FROM VehicleTyreFitting f WHERE f.tenant.id = :tenantId AND f.removedDate IS NULL AND f.isActive = true")
    List<VehicleTyreFitting> findAllActiveFittingsByTenantId(@Param("tenantId") Long tenantId);

    // All fittings (active + historical) for a tenant — for km/cost analysis
    @Query("SELECT f FROM VehicleTyreFitting f WHERE f.tenant.id = :tenantId AND f.isActive = true ORDER BY f.fittedDate DESC")
    List<VehicleTyreFitting> findAllFittingsByTenantId(@Param("tenantId") Long tenantId);
}
