package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CorrectSubscriptionRequest {

    private String planName;            // optional — override plan label

    @Min(1)
    private Integer vehicleCount;       // optional — if null, keeps existing

    private BigDecimal pricePerVehicle; // optional — override price per vehicle

    private String billingCycle;        // optional — BillingCycle name (MONTHLY, etc.)

    private LocalDate endDate;          // optional — new expiry date

    private String paymentRef;          // optional

    private BigDecimal amount;          // optional override — recalculates if null

    @NotBlank
    private String notes;               // required — reason for correction
}
