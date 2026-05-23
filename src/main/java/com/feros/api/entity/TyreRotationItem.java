package com.feros.api.entity;

import com.feros.api.util.TimeUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tyre_rotation_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TyreRotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotation_log_id", nullable = false)
    private TyreRotationLog rotationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tyre_id", nullable = false)
    private Tyre tyre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_position_id", nullable = false)
    private VehicleTyrePosition fromPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_position_id", nullable = false)
    private VehicleTyrePosition toPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_fitting_id", nullable = false)
    private VehicleTyreFitting oldFitting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_fitting_id", nullable = false)
    private VehicleTyreFitting newFitting;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtil.nowIst();
    }
}
