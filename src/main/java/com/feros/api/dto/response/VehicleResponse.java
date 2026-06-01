package com.feros.api.dto.response;

import com.feros.api.enums.TripScope;
import com.feros.api.enums.VehicleStatusType;
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
    private String model;

    private Long vehicleTypeId;
    private String vehicleTypeName;

    private Long fuelTypeId;
    private String fuelTypeName;

    private Long ownershipTypeId;
    private String ownershipTypeName;

    private Long currentStatusId;
    private String currentStatusName;
    private VehicleStatusType currentStatusType;

    private BigDecimal capacityInTons;
    private BigDecimal grossVehicleWeight;
    private Integer manufactureYear;
    private String color;
    private String chassisNumber;
    private String engineNumber;

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
    private BigDecimal fuelTankCapacity;
    private BigDecimal currentFuelLevel;
    // Finance
    private Boolean isFinanced;
    private String financerName;
    private LocalDate financeStartDate;
    private LocalDate financeEndDate;
    private Integer financeMonthsRemaining;

    private String notes;
    private Integer tyreRotationIntervalKm;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Extra pay for assigned driver
    private Boolean extraPayEnabled;
    private BigDecimal extraPayPerDay;

    // Vehicle-level staff assignment
    private Long currentDriverId;
    private String currentDriverName;
    private Long currentCleanerId;
    private String currentCleanerName;

    // Date-based assignment info (populated when ?date= param is provided)
    private Boolean isAssigned;
    private Long assignedOrderId;
    private String assignedOrderNumber;

    private String coverImageUrl;

    private TripScope tripScope;
}