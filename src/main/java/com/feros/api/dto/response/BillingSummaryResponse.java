package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BillingSummaryResponse {
    private BigDecimal machineRentalAmount;
    private BigDecimal operatorAmount;
    private BigDecimal mobilizationCharge;
    private BigDecimal demobilizationCharge;
    private BigDecimal totalAmount;
    private BigDecimal totalHours;   // for HOURLY / DAILY_SHIFT
    private Integer totalWorkingDays;
}
