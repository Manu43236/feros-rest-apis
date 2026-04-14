package com.feros.api.repository;

import com.feros.api.entity.TireRotationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TireRotationLogRepository extends JpaRepository<TireRotationLog, Long> {

    List<TireRotationLog> findByVehicleIdAndIsActiveTrueOrderByRotationDateDescIdDesc(Long vehicleId);
}
