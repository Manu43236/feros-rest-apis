package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CorrectSubscriptionRequest {

    @Min(1)
    private Integer vehicleCount;   // optional — if null, keeps existing

    private String paymentRef;      // optional

    private BigDecimal amount;      // optional override — recalculates if null

    @NotBlank
    private String notes;           // required — reason for correction
}
