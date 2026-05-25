package com.feros.api.entity;

import com.feros.api.entity.master.*;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_brand_id")
    private VehicleBrand brand;

    @Column(name = "model")
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuel_type_id")
    private FuelType fuelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownership_type_id")
    private OwnershipType ownershipType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_status_id")
    private VehicleStatus currentStatus;

    @Column(name = "capacity_in_tons")
    private BigDecimal capacityInTons;

    @Column(name = "gross_vehicle_weight")
    private BigDecimal grossVehicleWeight;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "color")
    private String color;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "engine_number")
    private String engineNumber;

    // Attached owner info (for hired vehicles)
    @Column(name = "attached_owner_name")
    private String ownerName;

    @Column(name = "attached_owner_phone")
    private String ownerPhone;

    @Column(name = "attached_owner_address", columnDefinition = "TEXT")
    private String ownerAddress;

    @Column(name = "agreement_start_date")
    private LocalDate agreementStartDate;

    @Column(name = "agreement_end_date")
    private LocalDate agreementEndDate;

    @Column(name = "agreement_amount")
    private BigDecimal agreementAmount;

    @Column(name = "owner_pan")
    private String ownerPan;

    // GPS
    @Column(name = "gps_device_number")
    private String gpsDeviceNumber;

    @Column(name = "gps_device_imei")
    private String gpsDeviceImei;

    @Column(name = "gps_provider")
    private String gpsProvider;

    // Odometer
    @Column(name = "current_odometer_reading")
    private BigDecimal currentOdometerReading;

    // Fuel
    @Column(name = "fuel_tank_capacity")
    private BigDecimal fuelTankCapacity;

    @Column(name = "current_fuel_level")
    private BigDecimal currentFuelLevel;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tyre_rotation_interval_km")
    private Integer tyreRotationIntervalKm;

    @Column(name = "is_active")
    private Boolean isActive = true;
}