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
    private Integer vehicleCount;       // optional — keeps existing count if omitted

    private BigDecimal pricePerVehicle; // optional — override rate; keeps previous if omitted

    private String planName;            // optional — override plan label; keeps previous if omitted

    private LocalDate newEndDate;       // optional — auto-calculated from billing cycle if omitted
    private BigDecimal amount;          // optional override total base amount
    private String paymentRef;
    private String notes;
}
