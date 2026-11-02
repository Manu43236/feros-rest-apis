package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMachineAttendanceReport {
    private LocalDate date;
    private int totalMachines;
    private int operators;
    private int cleaners;
    private int unassigned;
    private int empty;
    private List<DailyMachineAttendanceRow> rows;
}
