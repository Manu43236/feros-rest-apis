package com.feros.api.entity;

import com.feros.api.entity.master.GpsDeviceModel;
import com.feros.api.enums.GpsDeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gps_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsDevice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private GpsDeviceModel model;

    // Primary lookup key — IMEI for TCP, clientId for REST_API, etc. Globally unique.
    @Column(name = "device_identifier", nullable = false, unique = true, length = 100)
    private String deviceIdentifier;

    // JSON blob — shape depends on connectionType. Never returned to frontend.
    @Column(name = "credentials", columnDefinition = "TEXT")
    private String credentials;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GpsDeviceStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_ping_at")
    private LocalDateTime lastPingAt;
}
