package com.feros.api.repository;

import com.feros.api.entity.VehicleTyrePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleTyrePositionRepository extends JpaRepository<VehicleTyrePosition, Long> {

    List<VehicleTyrePosition> findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(Long vehicleId);

    Optional<VehicleTyrePosition> findByVehicleIdAndPositionCodeAndIsActiveTrue(Long vehicleId, String positionCode);

    boolean existsByVehicleIdAndIsActiveTrue(Long vehicleId);
}
