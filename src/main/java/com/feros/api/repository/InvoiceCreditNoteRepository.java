package com.feros.api.repository;

import com.feros.api.entity.InvoiceCreditNote;
import com.feros.api.enums.CreditNoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceCreditNoteRepository extends JpaRepository<InvoiceCreditNote, Long> {
    List<InvoiceCreditNote> findByTenantIdAndIsActiveTrueOrderByCreditNoteDateDesc(Long tenantId);
    List<InvoiceCreditNote> findByTenantIdAndClientIdAndIsActiveTrueOrderByCreditNoteDateDesc(Long tenantId, Long clientId);
    List<InvoiceCreditNote> findByTenantIdAndCreditNoteStatusAndIsActiveTrue(Long tenantId, CreditNoteStatus status);
    Optional<InvoiceCreditNote> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
