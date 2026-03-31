package com.feros.api.entity;

import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.VehicleServiceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "service_number")
    private String serviceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false)
    private ServiceTriggeredBy triggeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakdown_id")
    private VehicleBreakdown breakdown;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private VehicleServiceType serviceType;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.OPEN;

    @Column(name = "due_at_odometer")
    private Integer dueAtOdometer;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "odometer")
    private Integer odometer;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<VehicleServiceTask> tasks = new ArrayList<>();
}
