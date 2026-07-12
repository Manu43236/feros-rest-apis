package com.feros.api.entity;

import com.feros.api.enums.DailyLogStatus;
import com.feros.api.enums.IdleAttribution;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment_daily_logs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"machine_assignment_id", "log_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDailyLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    // denormalized for easy querying without joining through machineAssignment
    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DailyLogStatus status;

    // Aggregate computed from division lines: min(startHMR), max(endHMR), sum(hours)
    @Column(name = "start_hour_meter", precision = 10, scale = 2)
    private BigDecimal startHourMeter;

    @Column(name = "end_hour_meter", precision = 10, scale = 2)
    private BigDecimal endHourMeter;

    @Column(name = "hours_worked", precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "fuel_consumed", precision = 10, scale = 2)
    private BigDecimal fuelConsumed;

    @Column(name = "notes")
    private String notes;

    // MANUAL = office staff entered | AUTO = midnight scheduler generated
    @Column(name = "source", nullable = false)
    @Builder.Default
    private String source = "MANUAL";

    // E4 — hour breakdown
    @Column(name = "working_hours", precision = 10, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "idle_hours", precision = 10, scale = 2)
    private BigDecimal idleHours;

    @Column(name = "standby_hours", precision = 10, scale = 2)
    private BigDecimal standbyHours;

    @Column(name = "breakdown_hours", precision = 10, scale = 2)
    private BigDecimal breakdownHours;

    // E4 — idle attribution (CLIENT_FAULT = billable idle; OUR_BREAKDOWN = not billable)
    @Enumerated(EnumType.STRING)
    @Column(name = "idle_attribution")
    private IdleAttribution idleAttribution;

    @Column(name = "idle_reason")
    private String idleReason;

    // E4 — client-signed slip photo URL (proof of billing)
    @Column(name = "signed_slip_photo_url")
    private String signedSlipPhotoUrl;

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DailyLogDivision> divisions = new ArrayList<>();
}
