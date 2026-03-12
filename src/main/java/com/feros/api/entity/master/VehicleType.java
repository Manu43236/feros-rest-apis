package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "capacity_in_tons", nullable = false)
    private BigDecimal capacityInTons;

    @Column(name = "tyre_count")
    private Integer tyreCount;

    @Column(name = "is_active")
    private Boolean isActive = true;
}