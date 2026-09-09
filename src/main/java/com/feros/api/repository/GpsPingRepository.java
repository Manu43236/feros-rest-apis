package com.feros.api.repository;

import com.feros.api.gps.model.GpsPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GpsPingRepository extends JpaRepository<GpsPing, Long> {

    Optional<GpsPing> findTopByVehicleIdOrderByRecordedAtUtcDesc(Long vehicleId);

    @Query("SELECT p FROM GpsPing p WHERE p.vehicleId = :vehicleId " +
           "AND p.recordedAtUtc BETWEEN :from AND :to ORDER BY p.recordedAtUtc ASC")
    List<GpsPing> findHistory(@Param("vehicleId") Long vehicleId,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    // Latest ping per vehicle for the fleet map — one row per vehicle
    @Query("SELECT p FROM GpsPing p WHERE p.tenantId = :tenantId " +
           "AND p.recordedAtUtc = (" +
           "  SELECT MAX(p2.recordedAtUtc) FROM GpsPing p2 WHERE p2.vehicleId = p.vehicleId)")
    List<GpsPing> findLatestPerVehicleForTenant(@Param("tenantId") Long tenantId);

    boolean existsByDeviceIdAndRecordedAtUtc(Long deviceId, LocalDateTime recordedAtUtc);
}
