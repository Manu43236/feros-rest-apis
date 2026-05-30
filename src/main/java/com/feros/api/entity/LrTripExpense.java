package com.feros.api.entity;

import com.feros.api.enums.TripExpenseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lr_trip_expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrTripExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lr_id", nullable = false, unique = true)
    private Lr lr;

    @Builder.Default
    @Column(name = "advance_amount", nullable = false)
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "trip_days", nullable = false)
    private Integer tripDays = 1;

    @Builder.Default
    @Column(name = "driver_batta", nullable = false)
    private BigDecimal driverBatta = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "cleaner_batta", nullable = false)
    private BigDecimal cleanerBatta = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "trip_mamulu", nullable = false)
    private BigDecimal tripMamulu = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripExpenseStatus status = TripExpenseStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private User submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "settlement_amount")
    private BigDecimal settlementAmount;

    @Column(name = "settlement_note", columnDefinition = "TEXT")
    private String settlementNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settled_by_id")
    private User settledBy;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
