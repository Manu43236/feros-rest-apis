package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderRouteSummaryRow {
    private String fromCity;
    private String fromState;
    private String toCity;
    private String toState;
    private int totalOrders;
    private int completedOrders;
    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private BigDecimal totalFreightAmount;
}
