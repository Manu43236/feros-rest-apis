package com.feros.api.repository;

import com.feros.api.entity.EquipmentFuelLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentFuelLogRepository extends JpaRepository<EquipmentFuelLog, Long> {
    List<EquipmentFuelLog> findByEquipmentIdAndTenantIdOrderByFillDateDescIdDesc(Long equipmentId, Long tenantId);
    Optional<EquipmentFuelLog> findByIdAndTenantId(Long id, Long tenantId);
}
