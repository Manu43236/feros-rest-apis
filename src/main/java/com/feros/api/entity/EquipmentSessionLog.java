package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_session_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentSessionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    // The logged-in OPERATOR user's id
    @Column(name = "operator_user_id", nullable = false)
    private Long operatorUserId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "start_hmr", precision = 10, scale = 2, nullable = false)
    private BigDecimal startHmr;

    // null = session still open
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "end_hmr", precision = 10, scale = 2)
    private BigDecimal endHmr;

    @Column(name = "fuel_consumed", precision = 10, scale = 2)
    private BigDecimal fuelConsumed;

    @Column(name = "notes")
    private String notes;
}
