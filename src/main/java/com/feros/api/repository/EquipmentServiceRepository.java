package com.feros.api.repository;

import com.feros.api.entity.EquipmentServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentServiceRepository extends JpaRepository<EquipmentServiceRecord, Long> {
    List<EquipmentServiceRecord> findByEquipmentIdAndTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long equipmentId, Long tenantId);
    Optional<EquipmentServiceRecord> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    // Fleet-wide — all service records for the tenant (service-manager console).
    List<EquipmentServiceRecord> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);
}
