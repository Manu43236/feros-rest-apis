package com.feros.api.repository;

import com.feros.api.entity.LeaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaseInvoiceRepository extends JpaRepository<LeaseInvoice, Long> {
    List<LeaseInvoice> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<LeaseInvoice> findByLease_IdAndTenantIdOrderByCreatedAtDesc(Long leaseId, Long tenantId);
    Optional<LeaseInvoice> findByIdAndTenantId(Long id, Long tenantId);
}
