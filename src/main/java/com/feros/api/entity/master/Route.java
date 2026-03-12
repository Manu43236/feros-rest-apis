package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_city_id", nullable = false)
    private City sourceCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @Column(name = "distance_in_km")
    private BigDecimal distanceInKm;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "is_active")
    private Boolean isActive = true;
}