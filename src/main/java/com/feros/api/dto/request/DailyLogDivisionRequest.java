package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DailyLogDivisionRequest {
    private Long divisionId;          // optional — used to look up name
    private BigDecimal startHourMeter;
    private BigDecimal endHourMeter;
    private String notes;
}
