package com.feros.api.gps.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gps_route_points", indexes = {
    @Index(name = "idx_rp_vehicle_time", columnList = "vehicle_id, recorded_at_utc")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GpsRoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id",      nullable = false) private Long       vehicleId;
    @Column(name = "tenant_id",       nullable = false) private Long       tenantId;
    @Column(name = "latitude",        nullable = false, precision = 9, scale = 6) private BigDecimal latitude;
    @Column(name = "longitude",       nullable = false, precision = 9, scale = 6) private BigDecimal longitude;
    @Column(name = "heading")                           private Integer    heading;
    @Column(name = "speed_kmh",       precision = 5, scale = 1) private BigDecimal speedKmh;
    @Column(name = "recorded_at_utc", nullable = false) private LocalDateTime recordedAtUtc;
}
