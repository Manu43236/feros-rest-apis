package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailyMachineRegisterRow {
    private LocalDate logDate;
    private String machine;         // equipment reg/serial
    private String equipmentType;
    private String woNumber;
    private String clientName;
    private String status;
    private BigDecimal startHourMeter;
    private BigDecimal endHourMeter;
    private BigDecimal hoursWorked;
    private BigDecimal workingHours;
    private BigDecimal idleHours;
    private BigDecimal standbyHours;
    private BigDecimal breakdownHours;
    private BigDecimal fuelConsumed;
    private String idleReason;
}
