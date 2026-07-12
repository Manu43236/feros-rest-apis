package com.feros.api.entity;

import com.feros.api.entity.master.EquipmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.enums.HireRateUnit;
import com.feros.api.enums.MeterType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "color")
    private String color;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "engine_number")
    private String engineNumber;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "fuel_tank_capacity", precision = 10, scale = 2)
    private BigDecimal fuelTankCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false)
    private EquipmentOwnershipType ownershipType;

    // Finance (OWNED only)
    @Column(name = "is_financed")
    @Builder.Default
    private Boolean isFinanced = false;

    @Column(name = "financer_name")
    private String financerName;

    @Column(name = "finance_start_date")
    private LocalDate financeStartDate;

    @Column(name = "finance_end_date")
    private LocalDate financeEndDate;

    // Hire info (HIRED_IN only)
    @Column(name = "hired_from")
    private String hiredFrom;

    @Column(name = "hire_start_date")
    private LocalDate hireStartDate;

    @Column(name = "hire_end_date")
    private LocalDate hireEndDate;

    @Column(name = "hire_rate", precision = 10, scale = 2)
    private BigDecimal hireRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "hire_rate_unit")
    private HireRateUnit hireRateUnit;

    // Meter
    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false)
    private MeterType meterType;

    @Column(name = "current_meter_reading", precision = 10, scale = 2)
    private BigDecimal currentMeterReading;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    @Builder.Default
    private EquipmentWorkStatus workStatus = EquipmentWorkStatus.AVAILABLE;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "monthly_depreciation", precision = 10, scale = 2)
    private BigDecimal monthlyDepreciation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
