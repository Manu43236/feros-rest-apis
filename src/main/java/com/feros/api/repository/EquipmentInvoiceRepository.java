package com.feros.api.repository;

import com.feros.api.entity.EquipmentInvoice;
import com.feros.api.enums.EquipmentInvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentInvoiceRepository extends JpaRepository<EquipmentInvoice, Long> {

    @Query("SELECT DISTINCT i FROM EquipmentInvoice i JOIN i.items item " +
           "WHERE i.tenant.id = :tenantId " +
           "AND item.machineAssignmentId IN " +
           "  (SELECT a.id FROM MachineAssignment a WHERE a.workOrder.id = :woId) " +
           "ORDER BY i.createdAt DESC")
    List<EquipmentInvoice> findByWorkOrderViaItems(@Param("tenantId") Long tenantId,
                                                   @Param("woId") Long woId);

    @Query("SELECT i FROM EquipmentInvoice i WHERE i.tenant.id = :tenantId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:clientId IS NULL OR i.client.id = :clientId)")
    Page<EquipmentInvoice> findAllPaged(@Param("tenantId") Long tenantId,
                                        @Param("status") EquipmentInvoiceStatus status,
                                        @Param("clientId") Long clientId,
                                        Pageable pageable);

    Optional<EquipmentInvoice> findByIdAndTenantId(Long id, Long tenantId);
}
