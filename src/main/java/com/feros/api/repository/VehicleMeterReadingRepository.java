package com.feros.api.repository;

import com.feros.api.entity.VehicleMeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleMeterReadingRepository extends JpaRepository<VehicleMeterReading, Long> {

    List<VehicleMeterReading> findByVehicleIdAndTenantIdAndIsActiveTrueOrderByReadingKmDesc(Long vehicleId, Long tenantId);

    List<VehicleMeterReading> findByTenantIdAndIsActiveTrueOrderByRecordedAtDesc(Long tenantId);

    Optional<VehicleMeterReading> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    @Query("SELECT m FROM VehicleMeterReading m WHERE m.vehicle.id = :vehicleId AND m.isActive = true ORDER BY m.readingKm DESC")
    List<VehicleMeterReading> findTopByVehicleOrderByReadingKmDesc(@Param("vehicleId") Long vehicleId);

    java.util.Optional<VehicleMeterReading> findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
            Long lrId, com.feros.api.enums.MeterReadingType readingType);
}
