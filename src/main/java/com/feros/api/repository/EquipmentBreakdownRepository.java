package com.feros.api.repository;

import com.feros.api.entity.EquipmentBreakdown;
import com.feros.api.enums.EquipmentBreakdownStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentBreakdownRepository extends JpaRepository<EquipmentBreakdown, Long> {

    List<EquipmentBreakdown> findByEquipmentIdAndTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long equipmentId, Long tenantId);

    Optional<EquipmentBreakdown> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    // A machine can have at most one open breakdown (REPORTED or IN_REPAIR) at a time.
    Optional<EquipmentBreakdown> findFirstByEquipmentIdAndTenantIdAndStatusInAndIsActiveTrue(
            Long equipmentId, Long tenantId, List<EquipmentBreakdownStatus> statuses);
}
