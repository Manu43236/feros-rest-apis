package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mirror of {@link LeaseDriverAssignmentLog} for lease cleaners — same one-vehicle rule,
 *  same attendance/payroll vehicle resolution (lease vehicles never get a VSA row). */
@Entity
@Table(name = "lease_cleaner_assignment_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseCleanerAssignmentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_vehicle_assignment_id", nullable = false)
    private LeaseVehicleAssignment leaseVehicleAssignment;

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
