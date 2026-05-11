package com.feros.api.dto.request;

import com.feros.api.enums.BillingCycle;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpgradeRequestRequest {
    @NotNull(message = "Plan is required")
    private Long planId;

    @NotNull(message = "Vehicle count is required")
    @Min(value = 1, message = "At least 1 vehicle required")
    private Integer vehicleCount;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    private String notes;
}
