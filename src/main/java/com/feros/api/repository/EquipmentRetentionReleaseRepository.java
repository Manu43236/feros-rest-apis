package com.feros.api.repository;

import com.feros.api.entity.EquipmentRetentionRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EquipmentRetentionReleaseRepository extends JpaRepository<EquipmentRetentionRelease, Long> {

    List<EquipmentRetentionRelease> findByWorkOrderIdAndTenantId(Long workOrderId, Long tenantId);

    Optional<EquipmentRetentionRelease> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM EquipmentRetentionRelease r WHERE r.workOrder.id = :woId AND r.tenant.id = :tenantId")
    BigDecimal sumByWorkOrder(@Param("woId") Long woId, @Param("tenantId") Long tenantId);
}
