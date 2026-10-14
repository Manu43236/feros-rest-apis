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
    private boolean isLease;      // staff is on a lease (shift or standing) that day → show "Lease" tag
    private String shiftLabel;    // "Shift 1 (05:00–17:00)" for shift-lease staff, else null
    private LocalDate attendanceDate;
    private String attendanceType;
    private LocalDateTime markedAt;
    private LocalDateTime markedOutAt;
    private Double hoursWorked;
    private String approvalStatus;
    private String leaveType;
    private String remarks;
}
