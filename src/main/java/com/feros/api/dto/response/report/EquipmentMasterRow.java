package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EquipmentMasterRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String make;
    private String model;
    private String equipmentType;
    private String fuelType;
    private String ownershipType;
    private String workStatus;
    private String meterType;
    private BigDecimal currentMeterReading;
    private Integer manufactureYear;
    private BigDecimal fuelTankCapacity;
    private String chassisNumber;
    private String engineNumber;
    private Boolean isFinanced;
    private String financerName;
    private LocalDate financeFrom;
    private LocalDate financeTo;
    private String hiredFrom;
    private LocalDate hireStartDate;
    private LocalDate hireEndDate;
    private BigDecimal hireRate;
    private String hireRateUnit;
}
