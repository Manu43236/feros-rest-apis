package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tire_rotation_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TireRotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotation_log_id", nullable = false)
    private TireRotationLog rotationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tire_id", nullable = false)
    private Tire tire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_position_id", nullable = false)
    private VehicleTirePosition fromPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_position_id", nullable = false)
    private VehicleTirePosition toPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_fitting_id", nullable = false)
    private VehicleTireFitting oldFitting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_fitting_id", nullable = false)
    private VehicleTireFitting newFitting;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
