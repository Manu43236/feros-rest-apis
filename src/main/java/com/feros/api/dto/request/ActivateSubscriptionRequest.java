package com.feros.api.dto.request;

import com.feros.api.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ActivateSubscriptionRequest {
    @NotNull(message = "Plan ID is required")
    private Long planId;
    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    private BigDecimal amount;
    private String paymentRef;
    private String notes;
}
