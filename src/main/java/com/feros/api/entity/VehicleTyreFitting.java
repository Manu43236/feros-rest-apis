package com.feros.api.entity;

import com.feros.api.enums.TyreRemovalReason;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_tyre_fittings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleTyreFitting extends BaseEntity {

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
    @JoinColumn(name = "tyre_id", nullable = false)
    private Tyre tyre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private VehicleTyrePosition position;

    @Column(name = "fitted_at_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal fittedAtKm;

    @Column(name = "fitted_date", nullable = false)
    private LocalDate fittedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitted_by_user_id", nullable = false)
    private User fittedBy;

    @Column(name = "removed_at_km", precision = 12, scale = 2)
    private BigDecimal removedAtKm;

    @Column(name = "removed_date")
    private LocalDate removedDate;

    @Column(name = "removal_reason")
    @Enumerated(EnumType.STRING)
    private TyreRemovalReason removalReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "removed_by_user_id")
    private User removedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotation_log_id")
    private TyreRotationLog rotationLog;

    @Column(name = "km_driven", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal kmDriven;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
