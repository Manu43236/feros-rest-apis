package com.feros.api.entity;

import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.OperatorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "machine_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason")
    private AssignmentEndReason endReason;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_type")
    private OperatorType operatorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_staff_id")
    private StaffProfile operatorStaff;

    @Column(name = "hired_operator_name")
    private String hiredOperatorName;

    @Column(name = "hired_operator_phone")
    private String hiredOperatorPhone;

    @Column(name = "division_id")
    private Long divisionId;

    @Column(name = "division_name")
    private String divisionName;
}
