package com.feros.api.repository;

import com.feros.api.entity.VehicleService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleServiceRepository extends JpaRepository<VehicleService, Long> {
    List<VehicleService> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);
    List<VehicleService> findByTenantIdAndVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, Long vehicleId);
    Optional<VehicleService> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
