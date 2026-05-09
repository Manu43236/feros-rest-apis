package com.feros.api.repository;

import com.feros.api.entity.InvoiceLr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLrRepository extends JpaRepository<InvoiceLr, Long> {
    List<InvoiceLr> findByInvoiceIdAndIsActiveTrue(Long invoiceId);
    boolean existsByLrIdAndIsActiveTrue(Long lrId);

    @Query("SELECT il.lr.id FROM InvoiceLr il WHERE il.tenant.id = :tenantId AND il.isActive = true")
    List<Long> findActiveLrIds(@Param("tenantId") Long tenantId);

    List<InvoiceLr> findByTenantIdAndIsActiveTrue(Long tenantId);
}