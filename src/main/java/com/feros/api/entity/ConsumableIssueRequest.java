package com.feros.api.entity;

import com.feros.api.entity.master.SparePart;
import com.feros.api.enums.ConsumableRequestStatus;
import com.feros.api.util.TimeUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Supervisor requests a consumable (e.g. DEF/AdBlue) for a vehicle naming only the vehicle;
 * the store-keeper picks the actual spare part + quantity at issue time and draws down stock
 * attributed to that vehicle. Modeled on the shape of {@link TyreIssueRequest}.
 */
@Entity
@Table(name = "consumable_issue_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumableIssueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Set by the store-keeper at issue time (supervisor doesn't know the exact item).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id")
    private SparePart sparePart;

    @Column(name = "quantity_issued")
    private Integer quantityIssued;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ConsumableRequestStatus status = ConsumableRequestStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtil.nowIst();
        updatedAt = TimeUtil.nowIst();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimeUtil.nowIst();
    }
}
