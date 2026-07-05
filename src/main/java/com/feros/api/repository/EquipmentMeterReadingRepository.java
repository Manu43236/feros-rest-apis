package com.feros.api.repository;

import com.feros.api.entity.EquipmentMeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentMeterReadingRepository extends JpaRepository<EquipmentMeterReading, Long> {
    List<EquipmentMeterReading> findByEquipmentIdAndTenantIdOrderByReadingDateDescIdDesc(Long equipmentId, Long tenantId);
    Optional<EquipmentMeterReading> findByIdAndTenantId(Long id, Long tenantId);
}
