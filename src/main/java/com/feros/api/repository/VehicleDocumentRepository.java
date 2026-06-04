package com.feros.api.repository;

import com.feros.api.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {
    List<VehicleDocument> findByVehicleIdAndTenantIdAndIsActiveTrue(Long vehicleId, Long tenantId);
    Optional<VehicleDocument> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    @Query("SELECT vd FROM VehicleDocument vd WHERE vd.tenant.id = :tenantId AND vd.isActive = true AND vd.expiryDate IS NOT NULL AND vd.expiryDate <= :alertDate")
    List<VehicleDocument> findExpiringDocuments(@Param("tenantId") Long tenantId, @Param("alertDate") LocalDate alertDate);

    List<VehicleDocument> findByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT vd FROM VehicleDocument vd WHERE vd.tenant.id = :tenantId AND vd.isActive = true AND vd.paidOn IS NOT NULL AND vd.paidOn BETWEEN :from AND :to ORDER BY vd.paidOn DESC")
    List<VehicleDocument> findByTenantIdAndPaidOnBetween(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}