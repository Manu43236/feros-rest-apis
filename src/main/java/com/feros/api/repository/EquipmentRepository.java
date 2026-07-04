package com.feros.api.repository;

import com.feros.api.entity.Equipment;
import com.feros.api.enums.EquipmentWorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByTenantId(Long tenantId);
    Optional<Equipment> findByIdAndTenantId(Long id, Long tenantId);
    long countByTenantId(Long tenantId);
    long countByTenantIdAndWorkStatus(Long tenantId, EquipmentWorkStatus workStatus);
    boolean existsBySerialNumberAndTenantId(String serialNumber, Long tenantId);
    boolean existsBySerialNumberAndTenantIdAndIdNot(String serialNumber, Long tenantId, Long id);
}
