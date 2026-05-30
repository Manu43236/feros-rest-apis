package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TripExpenseRequest {
    private BigDecimal advanceAmount;
    private Integer tripDays; // optional — auto-calculated from LR if null
    private List<TripExpenseItemRequest> items;
}
