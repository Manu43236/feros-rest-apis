package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceDailyRow {
    private Long employeeId;
    private String employeeName;
    private String role;
    private String vehicleRegistrationNumber;
    private LocalDate attendanceDate;
    private String attendanceType;
    private LocalDateTime markedAt;
    private LocalDateTime markedOutAt;
    private Double hoursWorked;
    private String approvalStatus;
    private String leaveType;
    private String remarks;
}
