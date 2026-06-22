package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class VehicleMasterRow {
    private Long vehicleId;
    private String registrationNumber;
    private String brand;
    private String model;
    private String vehicleType;
    private String fuelType;
    private String ownershipType;
    private String currentStatus;
    private BigDecimal capacityInTons;
    private BigDecimal grossVehicleWeight;
    private Integer manufactureYear;
    private BigDecimal fuelTankCapacity;
    private String chassisNumber;
    private String engineNumber;
    private String rcNumber;
    private LocalDate rcExpiry;
    private String insuranceNumber;
    private LocalDate insuranceExpiry;
    private LocalDate permitExpiry;
    private LocalDate fitnessExpiry;
    private String pucNumber;
    private LocalDate pucExpiry;
    private LocalDate roadTaxExpiry;
    private Boolean isFinanced;
    private String financerName;
    private LocalDate financeFrom;
    private LocalDate financeTo;
    private Boolean isIot;
}
