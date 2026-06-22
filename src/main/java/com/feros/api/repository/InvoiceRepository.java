package com.feros.api.repository;

import com.feros.api.entity.Invoice;
import com.feros.api.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND i.isActive = true " +
           "AND (:status IS NULL OR i.invoiceStatus = :status) " +
           "AND (:search IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(i.client.clientName) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Invoice> findAllPaged(@Param("tenantId") Long tenantId,
                               @Param("status") InvoiceStatus status,
                               @Param("search") String search,
                               Pageable pageable);
    List<Invoice> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Invoice> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    List<Invoice> findByClientIdAndTenantIdAndIsActiveTrue(Long clientId, Long tenantId);
    long countByTenantIdAndInvoiceStatusAndIsActiveTrue(Long tenantId, InvoiceStatus status);
    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.tenant.id = :tenantId AND i.invoiceStatus NOT IN ('PAID', 'CANCELLED') AND i.isActive = true")
    BigDecimal sumOutstandingBalanceByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.tenant.id = :tenantId AND i.isActive = true AND i.invoiceStatus != 'CANCELLED'")
    BigDecimal sumTotalRevenueByTenantId(@Param("tenantId") Long tenantId);
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND i.isActive = true AND i.balanceDue > 0 ORDER BY i.dueDate ASC")
    List<Invoice> findOutstandingInvoices(@Param("tenantId") Long tenantId);
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND i.isActive = true AND i.balanceDue > 0 AND i.client.id = :clientId ORDER BY i.dueDate ASC")
    List<Invoice> findOutstandingInvoicesByClient(@Param("tenantId") Long tenantId, @Param("clientId") Long clientId);

    @Query("SELECT i FROM Invoice i WHERE i.isActive = true AND i.invoiceStatus IN :statuses AND i.dueDate < :today")
    List<Invoice> findByStatusInAndDueDateBefore(@Param("statuses") List<InvoiceStatus> statuses, @Param("today") java.time.LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND i.isActive = true AND i.invoiceDate BETWEEN :from AND :to ORDER BY i.invoiceDate DESC")
    List<Invoice> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);
}