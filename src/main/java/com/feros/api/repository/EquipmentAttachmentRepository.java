package com.feros.api.repository;

import com.feros.api.entity.EquipmentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentAttachmentRepository extends JpaRepository<EquipmentAttachment, Long> {
    List<EquipmentAttachment> findByTenantIdOrderByNameAsc(Long tenantId);
    Optional<EquipmentAttachment> findByIdAndTenantId(Long id, Long tenantId);
}
