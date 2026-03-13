package com.feros.api.repository;

import com.feros.api.entity.StaffDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffDocumentRepository extends JpaRepository<StaffDocument, Long> {
    List<StaffDocument> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);
    Optional<StaffDocument> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    @Query("SELECT sd FROM StaffDocument sd WHERE sd.tenant.id = :tenantId AND sd.isActive = true AND sd.expiryDate IS NOT NULL AND sd.expiryDate <= :alertDate")
    List<StaffDocument> findExpiringDocuments(@Param("tenantId") Long tenantId, @Param("alertDate") LocalDate alertDate);
}