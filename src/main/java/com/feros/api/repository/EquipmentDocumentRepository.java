package com.feros.api.repository;

import com.feros.api.entity.EquipmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EquipmentDocumentRepository extends JpaRepository<EquipmentDocument, Long> {
    List<EquipmentDocument> findByEquipmentIdAndTenantIdAndIsActiveTrueOrderByExpiryDateAsc(Long equipmentId, Long tenantId);
    Optional<EquipmentDocument> findByIdAndTenantId(Long id, Long tenantId);
    // Expiring/expired alert feed (docs with no expiry date are naturally excluded)
    List<EquipmentDocument> findByTenantIdAndIsActiveTrueAndExpiryDateLessThanEqualOrderByExpiryDateAsc(Long tenantId, LocalDate cutoff);
}
