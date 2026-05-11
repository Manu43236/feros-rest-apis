package com.feros.api.entity;

import com.feros.api.enums.BillingCycle;
import com.feros.api.enums.UpgradeRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "upgrade_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UpgradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @Column(name = "vehicle_count")
    private Integer vehicleCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycle billingCycle;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UpgradeRequestStatus status = UpgradeRequestStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
