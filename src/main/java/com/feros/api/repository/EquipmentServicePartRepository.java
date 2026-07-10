package com.feros.api.repository;

import com.feros.api.entity.EquipmentServicePart;
import com.feros.api.enums.ServicePartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentServicePartRepository extends JpaRepository<EquipmentServicePart, Long> {
    List<EquipmentServicePart> findByServiceIdAndIsActiveTrue(Long serviceId);
    List<EquipmentServicePart> findByServiceTaskId(Long taskId);
    List<EquipmentServicePart> findByServiceTaskIdIn(List<Long> taskIds);
    Optional<EquipmentServicePart> findByIdAndService_TenantId(Long id, Long tenantId);
    List<EquipmentServicePart> findByStatusAndService_TenantIdAndIsActiveTrue(ServicePartStatus status, Long tenantId);
}
