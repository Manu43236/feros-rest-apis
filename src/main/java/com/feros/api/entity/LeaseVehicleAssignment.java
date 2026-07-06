package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lease_vehicle_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseVehicleAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id", nullable = false)
    private VehicleLease lease;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Optional — null when client provides their own driver
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_staff_id")
    private StaffProfile driverStaff;

    @Column(name = "rate_per_vehicle", precision = 12, scale = 2, nullable = false)
    private BigDecimal ratePerVehicle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "odometer_at_start")
    private BigDecimal odometerAtStart;

    @Column(name = "odometer_at_end")
    private BigDecimal odometerAtEnd;

    @Column(name = "division_id")
    private Long divisionId;

    @Column(name = "division_name")
    private String divisionName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notes")
    private String notes;
}
