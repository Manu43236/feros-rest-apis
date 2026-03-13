package com.feros.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BulkAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotEmpty(message = "At least one attendance entry is required")
    private List<AttendanceEntry> entries;

    @Getter
    @Setter
    public static class AttendanceEntry {
        @NotNull
        private Long userId;
        @NotNull
        private Long attendanceTypeId;
        private Long leaveTypeId;
        private String leaveReason;
        private String remarks;
    }
}