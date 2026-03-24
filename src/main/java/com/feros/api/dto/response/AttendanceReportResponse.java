package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AttendanceReportResponse {
    private Long userId;
    private String userName;
    private String userPhone;
    private String roleName;
    private int totalDays;
    private int presentDays;
    private int absentDays;
    private int halfDays;
    private int leaveDays;
    private BigDecimal attendancePercentage;
}
