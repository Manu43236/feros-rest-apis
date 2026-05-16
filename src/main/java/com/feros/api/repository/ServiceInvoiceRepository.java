package com.feros.api.repository;

import com.feros.api.entity.ServiceInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceInvoiceRepository extends JpaRepository<ServiceInvoice, Long> {
    Optional<ServiceInvoice> findByServiceIdAndIsActiveTrue(Long serviceId);
    Optional<ServiceInvoice> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    List<ServiceInvoice> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);
}
