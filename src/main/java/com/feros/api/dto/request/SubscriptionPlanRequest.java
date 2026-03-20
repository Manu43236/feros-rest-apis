package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;
    @NotNull private Integer maxLorries;
    @NotNull private Integer maxUsers;
    @NotNull private BigDecimal priceMonthly;
    @NotNull private BigDecimal priceYearly;
    private String features; // JSON string
}
