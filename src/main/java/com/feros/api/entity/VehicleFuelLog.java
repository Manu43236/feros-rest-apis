package com.feros.api.entity;

import com.feros.api.enums.FuelPaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_fuel_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFuelLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filled_by_user_id", nullable = false)
    private User filledBy;

    @Column(name = "fill_date", nullable = false)
    private LocalDateTime fillDate;

    @Column(name = "litres_filled", nullable = false, precision = 10, scale = 2)
    private BigDecimal litresFilled;

    @Column(name = "odometer_reading", nullable = false, precision = 10, scale = 2)
    private BigDecimal odometerReading;

    @Column(name = "cost_per_litre", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerLitre;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "is_full_tank", nullable = false)
    private Boolean isFullTank = false;

    @Column(name = "payment_mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private FuelPaymentMode paymentMode;

    @Column(name = "fuel_station_name")
    private String fuelStationName;

    @Column(name = "fuel_station_city")
    private String fuelStationCity;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
