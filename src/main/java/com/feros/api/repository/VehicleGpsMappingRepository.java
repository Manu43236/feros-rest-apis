package com.feros.api.repository;

import com.feros.api.entity.VehicleGpsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleGpsMappingRepository extends JpaRepository<VehicleGpsMapping, Long> {

    List<VehicleGpsMapping> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    List<VehicleGpsMapping> findByGpsProviderConfigIdAndIsActiveTrue(Long configId);

    Optional<VehicleGpsMapping> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    boolean existsByVehicleIdAndGpsProviderConfigId(Long vehicleId, Long configId);
}
