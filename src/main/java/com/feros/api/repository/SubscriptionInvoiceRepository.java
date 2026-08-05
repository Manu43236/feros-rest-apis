package com.feros.api.repository;

import com.feros.api.entity.SubscriptionInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, Long> {
    List<SubscriptionInvoice> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
    java.util.Optional<SubscriptionInvoice> findByIdAndTenant_Id(Long id, Long tenantId);
    java.util.Optional<SubscriptionInvoice> findBySubscriptionHistoryId(Long historyId);

    @Query("SELECT COUNT(i) FROM SubscriptionInvoice i WHERE YEAR(i.createdAt) = :year AND MONTH(i.createdAt) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(MAX(i.sequenceNo), 0) FROM SubscriptionInvoice i WHERE i.fyYear = :fyYear AND i.proformaNumber IS NOT NULL")
    int maxProformaSeq(@Param("fyYear") String fyYear);

    @Query("SELECT COALESCE(MAX(i.sequenceNo), 0) FROM SubscriptionInvoice i WHERE i.fyYear = :fyYear AND i.invoiceStatus = 'CONFIRMED'")
    int maxInvoiceSeq(@Param("fyYear") String fyYear);
}
