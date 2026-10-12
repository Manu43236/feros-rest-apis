package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class LeaseShiftAssignmentResponse {
    private Long id;
    private Long assignmentId;        // lease_vehicle_assignment id
    private Long vehicleId;
    private String registrationNumber;
    private Long shiftId;
    private Integer shiftNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate workDate;

    private Long driverStaffId;       // null = client's driver
    private String driverName;
    private boolean clientDriver;

    private Long cleanerStaffId;      // null = client's cleaner
    private String cleanerName;
    private boolean clientCleaner;

    private BigDecimal driverPay;
    private BigDecimal cleanerPay;
}
