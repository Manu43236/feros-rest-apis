package com.feros.api.entity;

import com.feros.api.enums.ServiceTaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_service_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentServiceTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private EquipmentServiceRecord serviceRecord;

    @Column(name = "task_type_id")
    private Long taskTypeId;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "frequency_hmr", precision = 10, scale = 2)
    private BigDecimal frequencyHmr;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceTaskStatus status = ServiceTaskStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Technician assignment (shared TECHNICIAN pool with vehicles) — mirrors VehicleServiceTask.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mechanic_id")
    private User assignedMechanic;

    @Column(name = "mechanic_started_at")
    private LocalDateTime mechanicStartedAt;

    @Column(name = "mechanic_closed_at")
    private LocalDateTime mechanicClosedAt;
}
