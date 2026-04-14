package com.feros.api.dto.request;

import com.feros.api.enums.PermitType;
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
    private Long vehicleModelId;
    private Long vehicleTypeId;
    private Long fuelTypeId;
    private Long ownershipTypeId;
    private Long currentStatusId;

    private BigDecimal capacityInTons;
    private Integer manufactureYear;
    private String color;
    private String chassisNumber;
    private String engineNumber;

    // RC
    private String rcNumber;
    private LocalDate rcExpiryDate;

    // Insurance
    private String insuranceCompanyName;
    private String insurancePolicyNumber;
    private LocalDate insuranceStartDate;
    private LocalDate insuranceExpiryDate;

    // Permit
    private String permitNumber;
    private PermitType permitType;
    private LocalDate permitStartDate;
    private LocalDate permitExpiryDate;

    // Fitness
    private String fitnessCertificateNumber;
    private LocalDate fitnessExpiryDate;

    // PUC
    private String pucNumber;
    private LocalDate pollutionExpiryDate;

    // Road Tax
    private LocalDate roadTaxPaidDate;
    private LocalDate roadTaxExpiryDate;

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
}