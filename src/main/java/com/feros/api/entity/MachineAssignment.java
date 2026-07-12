package com.feros.api.entity;

import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.HireType;
import com.feros.api.enums.OperatorType;
import com.feros.api.enums.ProviderSide;
import com.feros.api.enums.RateType;
import jakarta.persistence.*;

import java.math.BigDecimal;
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

    // Per-machine billing rate — overrides WO rate when set
    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type")
    private RateType rateType;

    @Column(name = "rate_amount", precision = 12, scale = 2)
    private BigDecimal rateAmount;

    // Optional attachment deployed on this machine line (nullable). Rate flows to billing (E7).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id")
    private EquipmentAttachment attachment;

    // KAN-17 per-machine billing terms
    @Enumerated(EnumType.STRING)
    @Column(name = "hire_type")
    private HireType hireType;

    @Column(name = "guaranteed_hours", precision = 8, scale = 2)
    private BigDecimal guaranteedHours;

    @Column(name = "overtime_rate", precision = 12, scale = 2)
    private BigDecimal overtimeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "diesel_by_whom")
    private ProviderSide dieselByWhom;

    // KAN-18 actual on/off-hire dates (may differ from startDate/endDate)
    @Column(name = "on_hire_date")
    private LocalDate onHireDate;

    @Column(name = "off_hire_date")
    private LocalDate offHireDate;

    // KAN-20 machine swap back-link (nullable — set when this line replaced another)
    @Column(name = "swapped_from_assignment_id")
    private Long swappedFromAssignmentId;
}
