package com.feros.api.repository;

import com.feros.api.gps.model.GpsRoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GpsRoutePointRepository extends JpaRepository<GpsRoutePoint, Long> {

    List<GpsRoutePoint> findByVehicleIdAndTenantIdAndRecordedAtUtcBetweenOrderByRecordedAtUtcAsc(
            Long vehicleId, Long tenantId, LocalDateTime from, LocalDateTime to);
}
