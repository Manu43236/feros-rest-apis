package com.feros.api.repository;

import com.feros.api.entity.EquipmentInvoiceItem;
import com.feros.api.enums.EquipmentInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Query("""
        SELECT COALESCE(SUM(i.amount), 0) FROM EquipmentInvoiceItem i
        JOIN i.invoice inv
        WHERE i.machineAssignmentId IN (
            SELECT a.id FROM MachineAssignment a
            WHERE a.equipment.id = :equipmentId AND a.workOrder.tenant.id = :tenantId
        )
        AND inv.status IN :statuses
        AND (inv.invoiceDate >= :from AND inv.invoiceDate <= :to)
        """)
    BigDecimal sumRevenueByEquipment(
        @Param("equipmentId") Long equipmentId,
        @Param("tenantId") Long tenantId,
        @Param("statuses") List<EquipmentInvoiceStatus> statuses,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
