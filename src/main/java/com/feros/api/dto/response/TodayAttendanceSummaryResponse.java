package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TodayAttendanceSummaryResponse {
    private int presentCount;
    private int absentCount;
    private int leaveCount;
    private int notMarkedCount;
    private int totalStaff;
    private List<TodayAttendanceRow> records;

    @Data
    @Builder
    public static class TodayAttendanceRow {
        private Long userId;
        private String userName;
        private String phone;
        private String roleName;
        private String attendanceStatus; // PRESENT, ABSENT, LEAVE, NOT_MARKED
    }
}
