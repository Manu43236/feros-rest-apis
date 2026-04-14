package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_models")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private VehicleBrand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tyre_count")
    private Integer tyreCount;

    @Column(name = "capacity_in_tons")
    private BigDecimal capacityInTons;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
