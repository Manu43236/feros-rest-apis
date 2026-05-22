package com.feros.api.repository;

import com.feros.api.entity.VehicleFuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VehicleFuelLogRepository extends JpaRepository<VehicleFuelLog, Long> {

    List<VehicleFuelLog> findByTenantIdAndVehicleIdAndIsActiveTrueOrderByFillDateDescIdDesc(
            Long tenantId, Long vehicleId);

    List<VehicleFuelLog> findByTenantIdAndIsActiveTrueOrderByFillDateDescIdDesc(Long tenantId);

    // Most recent fuel log for a vehicle on a given day (for meter reading sync)
    @Query("""
        SELECT f FROM VehicleFuelLog f
        WHERE f.vehicle.id = :vehicleId
          AND f.isActive = true
          AND f.fillDate >= :startOfDay
          AND f.fillDate < :endOfDay
        ORDER BY f.id DESC
        LIMIT 1
    """)
    Optional<VehicleFuelLog> findLatestForVehicleOnDate(
            @Param("vehicleId") Long vehicleId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Previous full tank fill-up before a given log id (for mileage calculation)
    @Query("""
        SELECT f FROM VehicleFuelLog f
        WHERE f.vehicle.id = :vehicleId
          AND f.isFullTank = true
          AND f.isActive = true
          AND f.id < :currentId
        ORDER BY f.id DESC
        LIMIT 1
    """)
    Optional<VehicleFuelLog> findPreviousFullTankFill(
            @Param("vehicleId") Long vehicleId,
            @Param("currentId") Long currentId);
}
