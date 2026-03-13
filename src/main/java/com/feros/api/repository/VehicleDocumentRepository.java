package com.feros.api.repository;

import com.feros.api.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {
    List<VehicleDocument> findByVehicleIdAndTenantIdAndIsActiveTrue(Long vehicleId, Long tenantId);
    Optional<VehicleDocument> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}