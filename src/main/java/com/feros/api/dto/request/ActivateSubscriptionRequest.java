package com.feros.api.dto.request;

import com.feros.api.enums.BillingCycle;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ActivateSubscriptionRequest {

    private String planName;           // free-text label (e.g. "Enterprise", "Custom")

    @NotNull(message = "Vehicle count is required")
    @Min(value = 1, message = "At least 1 vehicle required")
    private Integer vehicleCount;

    private BigDecimal pricePerVehicle; // optional — used for invoice line; if null, amount must be provided

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private BigDecimal amount;              // optional override; auto-calculated from pricePerVehicle if omitted
    private BigDecimal installationCharges; // optional one-time setup/installation fee
    private String paymentRef;
    private String notes;
}
