package com.feros.api.repository;

import com.feros.api.entity.Vehicle;
import com.feros.api.enums.VehicleStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<Vehicle> findByTenantId(Long tenantId);
    Optional<Vehicle> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    Optional<Vehicle> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByRegistrationNumberAndTenantId(String registrationNumber, Long tenantId);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);
    boolean existsByRegistrationNumberAndTenantIdAndIdNot(String registrationNumber, Long tenantId, Long id);
    boolean existsByRegistrationNumberAndOwnershipTypeNameContainingIgnoreCase(String registrationNumber, String ownershipNamePart);
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    long countByTenantIdAndIsActiveFalse(Long tenantId);

    boolean existsByCurrentDriver_IdAndIdNot(Long driverId, Long vehicleId);
    boolean existsByCurrentCleaner_IdAndIdNot(Long cleanerId, Long vehicleId);

    @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND (v.currentDriver.id = :userId OR v.currentCleaner.id = :userId)")
    java.util.List<Vehicle> findAssignedVehiclesByStaffUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.tenant.id = :tenantId AND v.isActive = true " +
           "AND v.currentStatus IS NOT NULL AND v.currentStatus.statusType = :type")
    long countByTenantIdAndIsActiveTrueAndStatusType(@Param("tenantId") Long tenantId,
                                                     @Param("type") VehicleStatusType type);

}