package com.feros.api.repository;

import com.feros.api.entity.master.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, Long> {
    List<VehicleStatus> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<VehicleStatus> findByIdAndTenantId(Long id, Long tenantId);
}