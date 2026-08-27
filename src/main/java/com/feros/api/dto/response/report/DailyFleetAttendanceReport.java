package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyFleetAttendanceReport {
    private LocalDate date;
    private String scope;          // "Local" or "Out Station"
    private int totalVehicles;
    private int drivers;
    private int cleaners;
    private int unassigned;
    private List<DailyFleetAttendanceRow> rows;
}
