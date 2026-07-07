package com.feros.api.entity;

import com.feros.api.enums.LeaseSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lease_vehicle_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseVehicleSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LeaseVehicleAssignment assignment;

    // Driver — own staff (id + snapshotted name) or null for client's driver
    @Column(name = "driver_staff_id")
    private Long driverStaffId;

    @Column(name = "driver_name")
    private String driverName;

    // Division — null when status is IDLE or BREAKDOWN
    @Column(name = "division_id")
    private Long divisionId;

    @Column(name = "division_name")
    private String divisionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaseSessionStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Computed on close: Duration.between(startTime, endTime).toMinutes() / 60.0
    @Column(name = "hours_worked", precision = 8, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "notes")
    private String notes;
}
