package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Builder
public class WorkOrderShiftResponse {
    private Long id;
    private Integer shiftNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal operatorPay;
    private BigDecimal cleanerPay;
}
