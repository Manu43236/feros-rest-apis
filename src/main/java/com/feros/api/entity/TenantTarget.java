package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tenant_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantTarget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "target_trips")
    private Integer targetTrips;

    @Column(name = "target_tons", precision = 12, scale = 2)
    private BigDecimal targetTons;
}
