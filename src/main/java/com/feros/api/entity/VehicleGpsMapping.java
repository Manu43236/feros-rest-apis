package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "vehicle_gps_mappings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "gps_provider_config_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleGpsMapping extends BaseEntity {

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
    @JoinColumn(name = "gps_provider_config_id", nullable = false)
    private GpsProviderConfig gpsProviderConfig;

    // The vehicle's ID in the GPS provider's system
    @Column(name = "provider_vehicle_id", nullable = false, length = 100)
    private String providerVehicleId;

    // Registration number as the GPS provider knows it (may differ slightly)
    @Column(name = "provider_reg_number", length = 50)
    private String providerRegNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
