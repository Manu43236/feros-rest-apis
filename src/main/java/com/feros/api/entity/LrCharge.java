package com.feros.api.entity;

import com.feros.api.entity.master.ChargeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lr_charges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrCharge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lr_id", nullable = false)
    private Lr lr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_type_id", nullable = false)
    private ChargeType chargeType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive = true;
}