package com.feros.api.repository;

import com.feros.api.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Vehicle> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByRegistrationNumberAndTenantId(String registrationNumber, Long tenantId);
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    @Query("SELECT v FROM Vehicle v WHERE v.tenant.id = :tenantId AND v.isActive = true AND (" +
           "v.insuranceExpiryDate <= :alertDate OR v.permitExpiryDate <= :alertDate OR " +
           "v.fitnessExpiryDate <= :alertDate OR v.pollutionExpiryDate <= :alertDate OR " +
           "v.roadTaxExpiryDate <= :alertDate OR v.rcExpiryDate <= :alertDate)")
    List<Vehicle> findVehiclesWithExpiringDocs(@Param("tenantId") Long tenantId, @Param("alertDate") LocalDate alertDate);
}