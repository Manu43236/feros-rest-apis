package com.feros.api.repository;

import com.feros.api.entity.VehicleLease;
import com.feros.api.enums.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleLeaseRepository extends JpaRepository<VehicleLease, Long> {

    Optional<VehicleLease> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    @Query("""
        SELECT vl FROM VehicleLease vl
        WHERE vl.tenant.id = :tenantId
          AND vl.isActive = true
          AND (:status IS NULL OR vl.status = :status)
          AND (:clientId IS NULL OR vl.client.id = :clientId)
        ORDER BY vl.createdAt DESC
    """)
    Page<VehicleLease> findAllPaged(
            @Param("tenantId") Long tenantId,
            @Param("status") LeaseStatus status,
            @Param("clientId") Long clientId,
            Pageable pageable);
}
