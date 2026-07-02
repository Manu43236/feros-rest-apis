package com.feros.api.repository;

import com.feros.api.entity.WorkOrder;
import com.feros.api.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @Query("SELECT w FROM WorkOrder w WHERE w.tenant.id = :tenantId AND w.isActive = true " +
           "AND (:status IS NULL OR w.status = :status) " +
           "AND (:clientId IS NULL OR w.client.id = :clientId)")
    Page<WorkOrder> findAllPaged(@Param("tenantId") Long tenantId,
                                 @Param("status") WorkOrderStatus status,
                                 @Param("clientId") Long clientId,
                                 Pageable pageable);

    Optional<WorkOrder> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);

    // For scheduler — all active IN_PROGRESS work orders across all tenants
    List<WorkOrder> findByStatusAndIsActiveTrue(WorkOrderStatus status);
}
