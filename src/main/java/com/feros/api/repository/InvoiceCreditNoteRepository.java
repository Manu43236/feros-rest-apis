package com.feros.api.repository;

import com.feros.api.entity.InvoiceCreditNote;
import com.feros.api.enums.CreditNoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceCreditNoteRepository extends JpaRepository<InvoiceCreditNote, Long> {
    List<InvoiceCreditNote> findByTenantIdAndIsActiveTrueOrderByCreditNoteDateDesc(Long tenantId);
    List<InvoiceCreditNote> findByTenantIdAndClientIdAndIsActiveTrueOrderByCreditNoteDateDesc(Long tenantId, Long clientId);
    List<InvoiceCreditNote> findByTenantIdAndCreditNoteStatusAndIsActiveTrue(Long tenantId, CreditNoteStatus status);
    Optional<InvoiceCreditNote> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    @Query("SELECT cn FROM InvoiceCreditNote cn WHERE cn.tenant.id = :tenantId AND cn.isActive = true AND cn.creditNoteDate BETWEEN :from AND :to ORDER BY cn.creditNoteDate DESC")
    List<InvoiceCreditNote> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId,
                                                        @Param("from") LocalDate from,
                                                        @Param("to") LocalDate to);
}
