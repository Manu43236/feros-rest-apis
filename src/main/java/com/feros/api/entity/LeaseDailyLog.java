package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lease_daily_logs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "log_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaseDailyLog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LeaseVehicleAssignment assignment;

    // denormalized for easy querying without joining through assignment
    @Column(name = "lease_id", nullable = false)
    private Long leaseId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    // aggregated from completed sessions for this day
    @Column(name = "total_hours", precision = 8, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "km_driven", precision = 10, scale = 2)
    private BigDecimal kmDriven;

    @Column(name = "session_count")
    private Integer sessionCount;

    // AUTO = midnight scheduler | MANUAL = user triggered
    @Column(name = "source", nullable = false)
    @Builder.Default
    private String source = "MANUAL";

    @Column(name = "notes")
    private String notes;
}
