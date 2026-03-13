package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AttendanceRequest {

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance type is required")
    private Long attendanceTypeId;

    private Long leaveTypeId;
    private String leaveReason;
    private String remarks;
}