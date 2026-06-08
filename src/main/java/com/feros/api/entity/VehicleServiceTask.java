package com.feros.api.entity;

import com.feros.api.entity.master.ServiceTaskType;
import com.feros.api.enums.ServiceTaskStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_service_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private VehicleService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id")
    private ServiceTaskType taskType;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "frequency_km")
    private Integer frequencyKm;

    @Column(name = "cost")
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mechanic_id")
    private User assignedMechanic;

    @Column(name = "mechanic_closed_at")
    private LocalDateTime mechanicClosedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceTaskStatus status = ServiceTaskStatus.PENDING;
}
