package com.feros.api.repository;

import com.feros.api.entity.StaffDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffDocumentRepository extends JpaRepository<StaffDocument, Long> {
    List<StaffDocument> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);
    Optional<StaffDocument> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}