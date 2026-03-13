package com.feros.api.repository;

import com.feros.api.entity.Lr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LrRepository extends JpaRepository<Lr, Long> {
    List<Lr> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Lr> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByVehicleAllocationId(Long vehicleAllocationId);
    List<Lr> findByOrderIdAndIsActiveTrue(Long orderId);
}