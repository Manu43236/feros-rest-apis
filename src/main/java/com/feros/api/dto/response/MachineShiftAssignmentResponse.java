package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class MachineShiftAssignmentResponse {
    private Long id;
    private Long assignmentId;        // machine_assignment id
    private Long equipmentId;
    private String registrationNumber;
    private String equipmentName;
    private Long shiftId;
    private Integer shiftNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate workDate;

    private Long operatorStaffId;     // null = client's operator
    private String operatorName;
    private boolean clientOperator;

    private Long cleanerStaffId;      // null = client's cleaner
    private String cleanerName;
    private boolean clientCleaner;

    private BigDecimal operatorPay;
    private BigDecimal cleanerPay;
}
