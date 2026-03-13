package com.feros.api.dto.response;

import com.feros.api.enums.PermitType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private Long id;
    private Long tenantId;
    private String registrationNumber;

    private Long brandId;
    private String brandName;

    private Long vehicleTypeId;
    private String vehicleTypeName;

    private Long fuelTypeId;
    private String fuelTypeName;

    private Long ownershipTypeId;
    private String ownershipTypeName;

    private Long currentStatusId;
    private String currentStatusName;

    private BigDecimal capacityInTons;
    private Integer manufactureYear;
    private String color;
    private String chassisNumber;
    private String engineNumber;

    // RC
    private String rcNumber;
    private LocalDate rcExpiryDate;
    private Boolean rcExpired;

    // Insurance
    private String insuranceCompanyName;
    private String insurancePolicyNumber;
    private LocalDate insuranceStartDate;
    private LocalDate insuranceExpiryDate;
    private Boolean insuranceExpired;

    // Permit
    private String permitNumber;
    private PermitType permitType;
    private LocalDate permitStartDate;
    private LocalDate permitExpiryDate;
    private Boolean permitExpired;

    // Fitness
    private String fitnessCertificateNumber;
    private LocalDate fitnessExpiryDate;
    private Boolean fitnessExpired;

    // PUC
    private String pucNumber;
    private LocalDate pollutionExpiryDate;
    private Boolean pollutionExpired;

    // Road Tax
    private LocalDate roadTaxPaidDate;
    private LocalDate roadTaxExpiryDate;
    private Boolean roadTaxExpired;

    // Owner info
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
    private String notes;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}