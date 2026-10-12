package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row per vehicle × shift × day — who worked a given shift on a given date, and what it paid.
 *  THE single anchor for shift attendance + payroll: {@code workDate} is the shift's START date
 *  (night shift 21:00→09:00 credits to the start date). Null driver/cleaner = client's own
 *  (tenant pays nothing). Pay is snapshotted from {@link LeaseShift} at assignment time so later
 *  edits to the shift's pay are forward-only and already-worked days keep their amount. */
@Entity
@Table(name = "lease_shift_assignments",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"lease_vehicle_assignment_id", "lease_shift_id", "work_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseShiftAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_vehicle_assignment_id", nullable = false)
    private LeaseVehicleAssignment leaseVehicleAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_shift_id", nullable = false)
    private LeaseShift leaseShift;

    // = shift start date (night shift credited here)
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // null = client's driver (no payroll)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_staff_id")
    private StaffProfile driverStaff;

    // null = client's cleaner (no payroll)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_staff_id")
    private StaffProfile cleanerStaff;

    // Snapshotted from LeaseShift at assignment time → forward-only pay edits
    @Column(name = "driver_pay_snapshot", precision = 12, scale = 2)
    private BigDecimal driverPaySnapshot;

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
