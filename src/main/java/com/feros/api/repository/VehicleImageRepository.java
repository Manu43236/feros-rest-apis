package com.feros.api.repository;

import com.feros.api.entity.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {
    List<VehicleImage> findByVehicleIdAndTenantIdAndIsActiveTrue(Long vehicleId, Long tenantId);
    Optional<VehicleImage> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
