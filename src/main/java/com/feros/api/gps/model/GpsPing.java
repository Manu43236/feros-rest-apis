package com.feros.api.gps.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gps_pings",
    indexes = {
        @Index(name = "idx_vehicle_time",  columnList = "vehicle_id, recorded_at_utc"),
        @Index(name = "idx_tenant_time",   columnList = "tenant_id, recorded_at_utc"),
        @Index(name = "idx_device_frame",  columnList = "device_id, frame_number")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_recorded", columnNames = {"device_id", "recorded_at_utc"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsPing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Denormalized for fast queries — avoids joins on every map load
    @Column(name = "device_id",  nullable = false) private Long deviceId;
    @Column(name = "vehicle_id", nullable = false) private Long vehicleId;
    @Column(name = "tenant_id",  nullable = false) private Long tenantId;

    @Column(name = "recorded_at_utc", nullable = false)
    private LocalDateTime recordedAtUtc;   // from device packet — always UTC

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;       // server wall clock

    @Column(name = "latitude",  nullable = false, precision = 9, scale = 6) private BigDecimal latitude;
    @Column(name = "longitude", nullable = false, precision = 9, scale = 6) private BigDecimal longitude;

    @Column(name = "speed_kmh", precision = 5, scale = 1) private BigDecimal speedKmh;
    @Column(name = "heading")                             private Integer    heading;
    @Column(name = "altitude",  precision = 7, scale = 1) private BigDecimal altitude;

    @Column(name = "ignition_on")    private Boolean ignitionOn;
    @Column(name = "gps_fix_valid")  private Boolean gpsFixValid;

    @Column(name = "packet_type", length = 5) private String  packetType;  // NR, IN, IF, HP, etc.
    @Column(name = "alert_id")                private Integer alertId;
    @Column(name = "is_history")              private Boolean isHistory;
    @Column(name = "frame_number")            private Integer frameNumber;

    @Column(name = "battery_voltage", precision = 4, scale = 1) private BigDecimal batteryVoltage;
    @Column(name = "main_voltage",    precision = 4, scale = 1) private BigDecimal mainVoltage;
    @Column(name = "gsm_signal")                                private Integer    gsmSignal;
}
