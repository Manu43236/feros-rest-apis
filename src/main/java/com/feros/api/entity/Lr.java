package com.feros.api.entity;

import com.feros.api.enums.LrStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lrs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "lr_number", nullable = false, unique = true)
    private String lrNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_allocation_id", nullable = false)
    private OrderVehicleAllocation vehicleAllocation;

    @Column(name = "lr_date", nullable = false)
    private LocalDate lrDate;

    @Column(name = "vehicle_capacity", nullable = false)
    private BigDecimal vehicleCapacity;

    @Column(name = "allocated_weight", nullable = false)
    private BigDecimal allocatedWeight;

    @Column(name = "loaded_weight")
    private BigDecimal loadedWeight;

    // overload_weight is GENERATED in DB — read only
    @Column(name = "overload_weight", insertable = false, updatable = false)
    private BigDecimal overloadWeight;

    @Column(name = "delivered_weight")
    private BigDecimal deliveredWeight;

    // weight_variance is GENERATED in DB — read only
    @Column(name = "weight_variance", insertable = false, updatable = false)
    private BigDecimal weightVariance;

    // is_overloaded is GENERATED in DB — read only
    @Column(name = "is_overloaded", insertable = false, updatable = false)
    private Boolean isOverloaded;

    @Column(name = "loaded_at")
    private LocalDateTime loadedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "lr_status")
    private LrStatus lrStatus = LrStatus.CREATED;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;
}