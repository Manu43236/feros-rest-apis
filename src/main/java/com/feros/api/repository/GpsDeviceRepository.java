package com.feros.api.repository;

import com.feros.api.entity.GpsDevice;
import com.feros.api.enums.GpsDeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpsDeviceRepository extends JpaRepository<GpsDevice, Long> {
    List<GpsDevice> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<GpsDevice> findByVehicleIdAndTenantIdAndStatus(Long vehicleId, Long tenantId, GpsDeviceStatus status);
    Optional<GpsDevice> findByDeviceIdentifier(String deviceIdentifier);

    // Eager-fetch model + vehicle + tenant for TCP thread (no Hibernate session available)
    @Query("SELECT d FROM GpsDevice d JOIN FETCH d.model JOIN FETCH d.vehicle JOIN FETCH d.tenant WHERE d.deviceIdentifier = :identifier AND d.status = 'ACTIVE'")
    Optional<GpsDevice> findActiveByDeviceIdentifierEager(@Param("identifier") String identifier);
}
