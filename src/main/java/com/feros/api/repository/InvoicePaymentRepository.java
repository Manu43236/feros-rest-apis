package com.feros.api.repository;

import com.feros.api.entity.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {
    List<InvoicePayment> findByInvoiceIdAndIsActiveTrue(Long invoiceId);
}