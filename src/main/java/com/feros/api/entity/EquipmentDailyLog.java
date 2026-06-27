package com.feros.api.entity;

import com.feros.api.enums.DailyLogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
}
