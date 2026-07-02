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

    List<EquipmentInvoice> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);

    @Query("SELECT i FROM EquipmentInvoice i WHERE i.tenant.id = :tenantId " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<EquipmentInvoice> findAllPaged(@Param("tenantId") Long tenantId,
                                        @Param("status") EquipmentInvoiceStatus status,
                                        Pageable pageable);

    Optional<EquipmentInvoice> findByIdAndTenantId(Long id, Long tenantId);
}
