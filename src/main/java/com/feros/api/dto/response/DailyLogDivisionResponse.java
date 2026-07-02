package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DailyLogDivisionResponse {
    private Long id;
    private String divisionName;
    private BigDecimal startHourMeter;
    private BigDecimal endHourMeter;
    private BigDecimal hoursWorked;
    private String notes;
}
