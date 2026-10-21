package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Builder
public class LeaseInvoicePrefillResponse {
    private Long leaseId;       // for grouping in client-level prefill
    private String leaseNumber; // for grouping in client-level prefill
    private Long assignmentId;
    private String registrationNumber;
    private String vehicleType;
    private LocalDate assignmentStart;
    private LocalDate assignmentEnd; // null = still active
    private Integer daysInPeriod;
    private BigDecimal rate;
    private BigDecimal suggestedAmount;
    private String rateType; // DAILY_SHIFT / MONTHLY

    // True when this lease-vehicle is already on a non-cancelled invoice overlapping the period.
    @lombok.Builder.Default
    private boolean alreadyInvoiced = false;
    private String invoicedInvoiceNumber; // the invoice it's already billed on
}
