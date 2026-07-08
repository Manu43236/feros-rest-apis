package com.feros.api.repository;

import com.feros.api.entity.VehicleTyrePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleTyrePositionRepository extends JpaRepository<VehicleTyrePosition, Long> {

    List<VehicleTyrePosition> findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(Long vehicleId);

    Optional<VehicleTyrePosition> findByVehicleIdAndPositionCodeAndIsActiveTrue(Long vehicleId, String positionCode);

    boolean existsByVehicleIdAndIsActiveTrue(Long vehicleId);
    boolean existsByVehicleId(Long vehicleId);
    @Modifying
    @Query("DELETE FROM VehicleTyrePosition v WHERE v.vehicle.id = :vehicleId")
    void deleteAllByVehicleId(@Param("vehicleId") Long vehicleId);
}
