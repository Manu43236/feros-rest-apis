package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderClientSummaryRow {
    private Long clientId;
    private String clientName;
    private int totalOrders;
    private int completedOrders;
    private int inProgressOrders;
    private int cancelledOrders;
    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private BigDecimal totalFreightAmount;
}
