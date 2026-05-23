package com.feros.api.repository;

import com.feros.api.entity.TyreRotationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TyreRotationLogRepository extends JpaRepository<TyreRotationLog, Long> {

    List<TyreRotationLog> findByVehicleIdAndIsActiveTrueOrderByRotationDateDescIdDesc(Long vehicleId);
}
