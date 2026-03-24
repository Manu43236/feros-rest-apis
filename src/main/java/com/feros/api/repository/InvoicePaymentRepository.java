package com.feros.api.repository;

import com.feros.api.entity.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {
    List<InvoicePayment> findByInvoiceIdAndIsActiveTrue(Long invoiceId);

    @Query("SELECT p FROM InvoicePayment p WHERE p.tenant.id = :tenantId AND p.isActive = true AND p.paymentDate BETWEEN :from AND :to ORDER BY p.paymentDate DESC")
    List<InvoicePayment> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT p FROM InvoicePayment p WHERE p.tenant.id = :tenantId AND p.isActive = true AND p.paymentDate BETWEEN :from AND :to AND p.invoice.client.id = :clientId ORDER BY p.paymentDate DESC")
    List<InvoicePayment> findByTenantIdAndDateRangeAndClient(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("clientId") Long clientId);

    @Query("SELECT p FROM InvoicePayment p WHERE p.tenant.id = :tenantId AND p.isActive = true AND p.invoice.client.id = :clientId AND p.paymentDate BETWEEN :from AND :to ORDER BY p.paymentDate ASC")
    List<InvoicePayment> findByTenantIdAndClientIdAndDateRange(@Param("tenantId") Long tenantId, @Param("clientId") Long clientId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}