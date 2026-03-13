package com.feros.api.repository;

import com.feros.api.entity.InvoiceLr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLrRepository extends JpaRepository<InvoiceLr, Long> {
    List<InvoiceLr> findByInvoiceIdAndIsActiveTrue(Long invoiceId);
    boolean existsByLrIdAndIsActiveTrue(Long lrId);
}