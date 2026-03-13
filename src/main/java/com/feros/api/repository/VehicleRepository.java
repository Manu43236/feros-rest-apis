package com.feros.api.repository;

import com.feros.api.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Vehicle> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByRegistrationNumberAndTenantId(String registrationNumber, Long tenantId);
}