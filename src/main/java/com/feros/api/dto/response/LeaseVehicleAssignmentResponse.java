package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LeaseVehicleAssignmentResponse {
    private Long id;
    private Long leaseId;
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private Long driverStaffId;
    private String driverName;
    private BigDecimal ratePerVehicle;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal odometerAtStart;
    private BigDecimal odometerAtEnd;
    private BigDecimal vehicleCurrentOdometer; // vehicle master odometer — fallback for session pre-fill
    private Long divisionId;
    private String divisionName;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
}
