package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkOwnAttendanceRequest {

    @NotNull(message = "Attendance type is required")
    private Long attendanceTypeId;

    private Long leaveTypeId;
    private String leaveReason;
    private String remarks;
}
