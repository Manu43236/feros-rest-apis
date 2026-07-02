package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "daily_log_divisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_log_id", nullable = false)
    private EquipmentDailyLog dailyLog;

    @Column(name = "division_name")
    private String divisionName;

    @Column(name = "start_hour_meter", precision = 10, scale = 2)
    private BigDecimal startHourMeter;

    @Column(name = "end_hour_meter", precision = 10, scale = 2)
    private BigDecimal endHourMeter;

    @Column(name = "hours_worked", precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "notes")
    private String notes;
}
