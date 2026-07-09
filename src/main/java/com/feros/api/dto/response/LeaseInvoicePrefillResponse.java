package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Builder
public class LeaseInvoicePrefillResponse {
    private Long assignmentId;
    private String registrationNumber;
    private String vehicleType;
    private LocalDate assignmentStart;
    private LocalDate assignmentEnd; // null = still active
    private Integer daysInPeriod;
    private BigDecimal rate;
    private BigDecimal suggestedAmount;
    private String rateType; // DAILY_SHIFT / MONTHLY
}
