package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanerPerformanceRow {
    private Long cleanerId;
    private String cleanerName;
    private int totalTrips;
    private BigDecimal totalWeight;
    private int presentDays;
    private int totalAttendanceDays;
    private BigDecimal attendancePct;
}
