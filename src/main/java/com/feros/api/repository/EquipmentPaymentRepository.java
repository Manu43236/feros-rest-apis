package com.feros.api.repository;

import com.feros.api.entity.EquipmentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EquipmentPaymentRepository extends JpaRepository<EquipmentPayment, Long> {

    List<EquipmentPayment> findByInvoiceIdAndTenantId(Long invoiceId, Long tenantId);

    Optional<EquipmentPayment> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM EquipmentPayment p WHERE p.invoice.id = :invoiceId AND p.tenant.id = :tenantId")
    BigDecimal sumByInvoice(@Param("invoiceId") Long invoiceId, @Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM EquipmentPayment p WHERE p.workOrderId = :woId AND p.tenant.id = :tenantId")
    BigDecimal sumByWorkOrder(@Param("woId") Long woId, @Param("tenantId") Long tenantId);

    List<EquipmentPayment> findByWorkOrderIdAndTenantId(Long workOrderId, Long tenantId);
}
