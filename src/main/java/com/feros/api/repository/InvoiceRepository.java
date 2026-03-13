package com.feros.api.repository;

import com.feros.api.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Invoice> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    List<Invoice> findByClientIdAndTenantIdAndIsActiveTrue(Long clientId, Long tenantId);
}