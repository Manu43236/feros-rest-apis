package com.feros.api.entity;

import com.feros.api.enums.BreakdownDuration;
import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.BreakdownType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_breakdowns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleBreakdown extends BaseEntity {

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
    @JoinColumn(name = "vehicle_allocation_id")
    private OrderVehicleAllocation vehicleAllocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "breakdown_date", nullable = false)
    private LocalDateTime breakdownDate;

    @Column(name = "location", length = 300)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "breakdown_type", nullable = false)
    private BreakdownType breakdownType;

    @Enumerated(EnumType.STRING)
    @Column(name = "breakdown_duration", nullable = false)
    private BreakdownDuration breakdownDuration;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BreakdownStatus status = BreakdownStatus.REPORTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_vehicle_allocation_id")
    private OrderVehicleAllocation replacementVehicleAllocation;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
