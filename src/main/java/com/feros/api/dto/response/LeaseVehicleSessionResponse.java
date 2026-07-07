package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LeaseVehicleSessionResponse {
    private Long id;
    private Long assignmentId;
    private Long vehicleId;
    private String registrationNumber;
    private Long driverStaffId;
    private String driverName;
    private Long divisionId;
    private String divisionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal hoursWorked;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
}
