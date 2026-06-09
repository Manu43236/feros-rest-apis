package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MechanicPerformanceRow {
    private Long mechanicId;
    private String mechanicName;
    private String designation;
    private int tasksAssigned;
    private int tasksCompleted;
    private int tasksMechanicClosed;
    private int tasksInProgress;
    private int servicesWorkedOn;
    private BigDecimal avgDurationMinutes;
}
