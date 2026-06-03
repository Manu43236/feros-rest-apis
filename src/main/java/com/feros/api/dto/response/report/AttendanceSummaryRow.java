package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AttendanceSummaryRow {
    private Long employeeId;
    private String employeeName;
    private String role;
    private String vehicleRegistrationNumber;
    private int presentDays;
    private int absentDays;
    private int leaveDays;
    private int halfDays;
    private int otherDays;
    private int totalRecords;
    private BigDecimal presentPercent;
}
