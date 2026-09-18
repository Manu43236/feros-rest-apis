package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceRoleSummaryRow {
    private String role;
    private int staffCount;
    private int pending;
    private int present;
    private int halfDay;
    private int onLeave;
    private int holiday;
    private int weekOff;
    private int absent;
    private int noAttendance;
}
