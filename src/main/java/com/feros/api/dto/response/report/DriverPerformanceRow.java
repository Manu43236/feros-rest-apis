package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPerformanceRow {
    private Long driverId;
    private String driverName;
    private int totalTrips;
    private BigDecimal totalWeight;
    private int deliveredTrips;
    private int onTimeDeliveries;
    private BigDecimal onTimePct;
    private int presentDays;
    private int totalAttendanceDays;
    private BigDecimal attendancePct;
}
