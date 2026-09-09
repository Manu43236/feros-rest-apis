package com.feros.api.repository;

import com.feros.api.entity.GpsDevice;
import com.feros.api.enums.GpsDeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpsDeviceRepository extends JpaRepository<GpsDevice, Long> {
    List<GpsDevice> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<GpsDevice> findByVehicleIdAndTenantIdAndStatus(Long vehicleId, Long tenantId, GpsDeviceStatus status);
    Optional<GpsDevice> findByDeviceIdentifier(String deviceIdentifier);
}
