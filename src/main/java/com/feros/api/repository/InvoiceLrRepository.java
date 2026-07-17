package com.feros.api.repository;

import com.feros.api.entity.InvoiceLr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceLrRepository extends JpaRepository<InvoiceLr, Long> {
    List<InvoiceLr> findByInvoiceIdAndIsActiveTrue(Long invoiceId);
    boolean existsByLrIdAndIsActiveTrue(Long lrId);

    @Query("SELECT il.lr.id FROM InvoiceLr il WHERE il.tenant.id = :tenantId AND il.isActive = true")
    List<Long> findActiveLrIds(@Param("tenantId") Long tenantId);

    List<InvoiceLr> findByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT DISTINCT il.invoice FROM InvoiceLr il WHERE il.order.id = :orderId AND il.isActive = true")
    List<com.feros.api.entity.Invoice> findDistinctInvoicesByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT il FROM InvoiceLr il WHERE il.tenant.id = :tenantId AND il.isActive = true AND il.invoice.invoiceDate BETWEEN :from AND :to")
    List<InvoiceLr> findByTenantIdAndInvoiceDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT il FROM InvoiceLr il WHERE il.tenant.id = :tenantId AND il.isActive = true AND il.lr.lrDate BETWEEN :from AND :to")
    List<InvoiceLr> findByTenantIdAndLrDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT il.invoice.id FROM InvoiceLr il WHERE il.lr.id = :lrId AND il.isActive = true")
    List<Long> findInvoiceIdsByLrId(@Param("lrId") Long lrId);

    @Query("SELECT il.invoice.invoiceNumber FROM InvoiceLr il WHERE il.lr.id = :lrId AND il.isActive = true")
    List<String> findInvoiceNumbersByLrId(@Param("lrId") Long lrId);
}