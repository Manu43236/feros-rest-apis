package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TripExpenseItemResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private BigDecimal approvedAmount;
    private String receiptUrl;
    private boolean amountChanged;
}
