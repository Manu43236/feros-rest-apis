package com.feros.api.repository;

import com.feros.api.entity.EquipmentInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentInvoiceItemRepository extends JpaRepository<EquipmentInvoiceItem, Long> {

    @Query("""
        SELECT i FROM EquipmentInvoiceItem i
        JOIN FETCH i.invoice inv
        JOIN FETCH inv.client
        WHERE i.machineAssignmentId IN (
            SELECT a.id FROM MachineAssignment a
            WHERE a.equipment.id = :equipmentId AND a.workOrder.tenant.id = :tenantId
        )
        ORDER BY inv.invoiceDate DESC
        """)
    List<EquipmentInvoiceItem> findByEquipmentIdAndTenantId(@Param("equipmentId") Long equipmentId, @Param("tenantId") Long tenantId);
}
