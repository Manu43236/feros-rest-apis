package com.feros.api.repository;

import com.feros.api.entity.VehicleTirePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleTirePositionRepository extends JpaRepository<VehicleTirePosition, Long> {

    List<VehicleTirePosition> findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(Long vehicleId);

    Optional<VehicleTirePosition> findByVehicleIdAndPositionCodeAndIsActiveTrue(Long vehicleId, String positionCode);
}
