package com.feros.api.repository;

import com.feros.api.entity.VehicleFuelLog;
import com.feros.api.enums.FuelPaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT f FROM VehicleFuelLog f
        WHERE f.tenant.id = :tenantId AND f.isActive = true
        AND (:vehicleId IS NULL OR f.vehicle.id = :vehicleId)
        AND (:paymentMode IS NULL OR f.paymentMode = :paymentMode)
        AND (:fullTank IS NULL OR f.isFullTank = :fullTank)
        AND (:search IS NULL
             OR LOWER(f.vehicle.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.fuelStationName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.fuelStationCity) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<VehicleFuelLog> findAllPaged(
            @Param("tenantId") Long tenantId,
            @Param("vehicleId") Long vehicleId,
            @Param("paymentMode") FuelPaymentMode paymentMode,
            @Param("fullTank") Boolean fullTank,
            @Param("search") String search,
            Pageable pageable);

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

    @Query("""
        SELECT f FROM VehicleFuelLog f
        WHERE f.tenant.id = :tenantId AND f.isActive = true
        AND f.fillDate >= :startDate AND f.fillDate < :endDate
        ORDER BY f.vehicle.id, f.fillDate
    """)
    List<VehicleFuelLog> findByTenantIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
