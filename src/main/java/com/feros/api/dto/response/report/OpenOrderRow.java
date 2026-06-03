package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OpenOrderRow {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String clientName;
    private String materialType;
    private String fromCity;
    private String toCity;
    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private BigDecimal pendingWeight;
    private String orderStatus;
}
