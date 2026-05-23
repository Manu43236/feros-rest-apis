package com.feros.api.entity;

import com.feros.api.enums.TyrePositionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_tyre_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleTyrePosition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "position_code", nullable = false)
    private String positionCode;

    @Column(name = "position_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TyrePositionType positionType;

    @Builder.Default
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
