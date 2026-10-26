package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/** A shift window defined on a work order (shift-duty work orders only). Up to 4 per work order, windows
 *  must not overlap; a window with start > end crosses midnight (e.g. 21:00–09:00). Pay follows the
 *  shift, snapshotted onto {@link MachineShiftAssignment} at assignment time. Equipment mirror of
 *  {@link LeaseShift}. */
@Entity
@Table(name = "work_order_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderShift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    // 1..4, display + ordering
    @Column(name = "shift_number", nullable = false)
    private Integer shiftNumber;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // start > end ⇒ crosses midnight
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "operator_pay", precision = 12, scale = 2, nullable = false)
    private BigDecimal operatorPay;

    // null when the work order has no cleaner on this shift
    @Column(name = "cleaner_pay", precision = 12, scale = 2)
    private BigDecimal cleanerPay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}
