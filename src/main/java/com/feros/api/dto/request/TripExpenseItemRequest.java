package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TripExpenseItemRequest {
    private String description;
    private BigDecimal amount;
    private String receiptUrl;
}
