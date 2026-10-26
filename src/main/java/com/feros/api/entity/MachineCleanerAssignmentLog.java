package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mirror of {@link MachineOperatorAssignmentLog} for machine cleaners — same one-machine rule,
 *  same attendance/payroll resolution. Equipment mirror of {@link LeaseCleanerAssignmentLog}.
 *  Null cleanerStaff = client's own cleaner (no payroll). */
@Entity
@Table(name = "machine_cleaner_assignment_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineCleanerAssignmentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    // null = client's cleaner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_staff_id")
    private StaffProfile cleanerStaff;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    // null = still active
    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}
