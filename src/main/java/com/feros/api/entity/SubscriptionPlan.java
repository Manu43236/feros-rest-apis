package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_lorries", nullable = false)
    private Integer maxLorries; // -1 = unlimited

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers; // -1 = unlimited

    @Column(name = "price_monthly", nullable = false)
    private BigDecimal priceMonthly;

    @Column(name = "price_yearly", nullable = false)
    private BigDecimal priceYearly;

    @Column(name = "price_per_vehicle", precision = 10, scale = 2)
    private BigDecimal pricePerVehicle;

    @Column(name = "min_vehicles")
    private Integer minVehicles;

    @Column(name = "max_vehicles")
    private Integer maxVehicles;

    // ─── Feature flags ──────────────────────────────────────────────────────────
    @Column(name = "has_fuel_logs")
    @Builder.Default private Boolean hasFuelLogs = true;

    @Column(name = "has_meter_readings")
    @Builder.Default private Boolean hasMeterReadings = true;

    @Column(name = "has_vehicle_services")
    @Builder.Default private Boolean hasVehicleServices = true;

    @Column(name = "has_attendance")
    @Builder.Default private Boolean hasAttendance = true;

    @Column(name = "has_payroll")
    @Builder.Default private Boolean hasPayroll = true;

    @Column(name = "has_inventory")
    @Builder.Default private Boolean hasInventory = true;

    @Column(name = "has_reports")
    @Builder.Default private Boolean hasReports = true;

    @Column(name = "has_credit_notes")
    @Builder.Default private Boolean hasCreditNotes = true;

    @Column(columnDefinition = "JSON")
    private String features;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
