package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    // Either materialTypeId (existing master) or customMaterialName (free-text "Other") must be provided
    private Long materialTypeId;
    private String customMaterialName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private Long unitId;
}
