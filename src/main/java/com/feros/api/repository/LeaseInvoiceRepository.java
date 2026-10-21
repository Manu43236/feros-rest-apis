package com.feros.api.repository;

import com.feros.api.entity.LeaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaseInvoiceRepository extends JpaRepository<LeaseInvoice, Long> {
    List<LeaseInvoice> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<LeaseInvoice> findByLease_IdAndTenantIdOrderByCreatedAtDesc(Long leaseId, Long tenantId);
    Optional<LeaseInvoice> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Lease-vehicle assignments already billed on a non-cancelled invoice whose billing period
     * overlaps [from, to], for this client. Returns [leaseVehicleAssignmentId, invoiceNumber] pairs.
     */
    @Query("""
        SELECT it.leaseVehicleAssignmentId, i.invoiceNumber
        FROM LeaseInvoice i JOIN i.items it
        WHERE i.client.id = :clientId
          AND i.tenant.id = :tenantId
          AND i.status <> com.feros.api.enums.EquipmentInvoiceStatus.CANCELLED
          AND it.leaseVehicleAssignmentId IS NOT NULL
          AND i.billingPeriodStart <= :to
          AND i.billingPeriodEnd >= :from
    """)
    List<Object[]> findInvoicedAssignments(
            @Param("clientId") Long clientId,
            @Param("tenantId") Long tenantId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
