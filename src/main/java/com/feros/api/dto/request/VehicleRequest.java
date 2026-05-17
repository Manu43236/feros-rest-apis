package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VehicleRequest {

    private String registrationNumber;

    private Boolean isActive;

    private Long brandId;
    private Long vehicleTypeId;
    private Long fuelTypeId;
    private Long ownershipTypeId;
    private Long currentStatusId;

    private BigDecimal capacityInTons;
    private Integer manufactureYear;
    private String color;
    private String chassisNumber;
    private String engineNumber;

    // Owner info (hired vehicles)
    private String ownerName;
    private String ownerPhone;
    private String ownerAddress;
    private String ownerPan;
    private LocalDate agreementStartDate;
    private LocalDate agreementEndDate;
    private BigDecimal agreementAmount;

    // GPS
    private String gpsDeviceNumber;
    private String gpsDeviceImei;
    private String gpsProvider;

    private BigDecimal currentOdometerReading;
    private BigDecimal fuelTankCapacity;
    private BigDecimal currentFuelLevel;
    private String notes;
    private Integer tyreRotationIntervalKm;
}
