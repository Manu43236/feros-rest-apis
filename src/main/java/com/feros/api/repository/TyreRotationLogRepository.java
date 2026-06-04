package com.feros.api.repository;

import com.feros.api.entity.TyreRotationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TyreRotationLogRepository extends JpaRepository<TyreRotationLog, Long> {

    List<TyreRotationLog> findByVehicleIdAndIsActiveTrueOrderByRotationDateDescIdDesc(Long vehicleId);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM TyreRotationLog r WHERE r.tenant.id = :tenantId AND r.isActive = true AND r.rotationDate BETWEEN :from AND :to ORDER BY r.rotationDate DESC")
    List<TyreRotationLog> findByTenantIdAndRotationDateBetween(@org.springframework.data.repository.query.Param("tenantId") Long tenantId, @org.springframework.data.repository.query.Param("from") java.time.LocalDate from, @org.springframework.data.repository.query.Param("to") java.time.LocalDate to);
}
