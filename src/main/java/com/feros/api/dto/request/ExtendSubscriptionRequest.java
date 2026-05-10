package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExtendSubscriptionRequest {
    @Min(value = 1, message = "At least 1 vehicle required")
    private Integer vehicleCount;      // optional — keeps existing count if omitted

    private LocalDate newEndDate;      // optional — auto-calculated from billing cycle if omitted
    private BigDecimal amount;         // optional override
    private String paymentRef;
    private String notes;
}
