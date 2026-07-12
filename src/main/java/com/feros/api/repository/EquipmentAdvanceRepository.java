package com.feros.api.repository;

import com.feros.api.entity.EquipmentAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EquipmentAdvanceRepository extends JpaRepository<EquipmentAdvance, Long> {

    List<EquipmentAdvance> findByWorkOrderIdAndTenantId(Long workOrderId, Long tenantId);

    Optional<EquipmentAdvance> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM EquipmentAdvance a WHERE a.workOrder.id = :woId AND a.tenant.id = :tenantId")
    BigDecimal sumByWorkOrder(@Param("woId") Long woId, @Param("tenantId") Long tenantId);
}
