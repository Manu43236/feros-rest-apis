package com.feros.api.repository;

import com.feros.api.entity.TenantDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantDocumentRepository extends JpaRepository<TenantDocument, Long> {
    List<TenantDocument> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<TenantDocument> findByIdAndIsActiveTrue(Long id);
}
