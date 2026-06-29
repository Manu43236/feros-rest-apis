package com.feros.api.entity;

import com.feros.api.enums.OperatorType;
import com.feros.api.enums.WorkEntryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_work_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineWorkEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_type")
    private OperatorType operatorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_staff_id")
    private StaffProfile operatorStaff;

    @Column(name = "hired_operator_name")
    private String hiredOperatorName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_meter", precision = 10, scale = 2)
    private BigDecimal startMeter;

    @Column(name = "end_meter", precision = 10, scale = 2)
    private BigDecimal endMeter;

    @Column(name = "hours_worked", precision = 8, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WorkEntryStatus status = WorkEntryStatus.ACTIVE;
}
