package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "odometer_reading")
    private BigDecimal odometerReading;

    @Column(name = "next_service_due_date")
    private LocalDate nextServiceDueDate;

    @Column(name = "next_service_due_odometer")
    private BigDecimal nextServiceDueOdometer;

    @Column(name = "service_center_name")
    private String serviceCenterName;

    @Column(name = "service_center_phone", length = 15)
    private String serviceCenterPhone;

    @Column(name = "bill_url", length = 500)
    private String billUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
