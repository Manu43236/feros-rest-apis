package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/** A shift window defined on a vehicle lease (shift-duty leases only). Up to 4 per lease, windows
 *  must not overlap; a window with start > end crosses midnight (e.g. 21:00–09:00). Pay follows the
 *  shift, snapshotted onto {@link LeaseShiftAssignment} at assignment time. */
@Entity
@Table(name = "lease_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseShift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id", nullable = false)
    private VehicleLease lease;

    // 1..4, display + ordering
    @Column(name = "shift_number", nullable = false)
    private Integer shiftNumber;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // start > end ⇒ crosses midnight
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "driver_pay", precision = 12, scale = 2, nullable = false)
    private BigDecimal driverPay;

    // null when the lease has no cleaner on this shift
    @Column(name = "cleaner_pay", precision = 12, scale = 2)
    private BigDecimal cleanerPay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}
