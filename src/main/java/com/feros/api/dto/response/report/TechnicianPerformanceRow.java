package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianPerformanceRow {
    private Long technicianId;
    private String technicianName;
    private String designation;
    private int tasksAssigned;
    private int tasksCompleted;
    private int tasksTechnicianClosed;
    private int tasksInProgress;
    private int servicesWorkedOn;
    private BigDecimal avgDurationMinutes;
}
