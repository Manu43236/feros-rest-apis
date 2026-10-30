package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row per machine × shift × day — who worked a given shift on a given date, and what it paid.
 *  THE single anchor for shift attendance + payroll: {@code workDate} is the shift's START date
 *  (night shift 21:00→09:00 credits to the start date). Null operator/cleaner = client's own
 *  (tenant pays nothing). Pay is snapshotted from {@link WorkOrderShift} at assignment time so later
 *  edits to the shift's pay are forward-only and already-worked days keep their amount. Equipment
 *  mirror of {@link LeaseShiftAssignment}. */
@Entity
@Table(name = "machine_shift_assignments",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"machine_assignment_id", "work_order_shift_id", "work_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineShiftAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_shift_id", nullable = false)
    private WorkOrderShift workOrderShift;

    // = shift start date (night shift credited here)
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // null = client's operator (no payroll)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_staff_id")
    private StaffProfile operatorStaff;

    @Column(name = "client_operator_name", length = 100)
    private String clientOperatorName;

    // null = client's cleaner (no payroll)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_staff_id")
    private StaffProfile cleanerStaff;

    @Column(name = "client_cleaner_name", length = 100)
    private String clientCleanerName;

    // Snapshotted from WorkOrderShift at assignment time → forward-only pay edits
    @Column(name = "operator_pay_snapshot", precision = 12, scale = 2)
    private BigDecimal operatorPaySnapshot;

    @Column(name = "cleaner_pay_snapshot", precision = 12, scale = 2)
    private BigDecimal cleanerPaySnapshot;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}
