package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OrdersBacklogResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private String clientName;
    private String fromCity;
    private String toCity;
    private String materialType;
    private BigDecimal totalWeight;
    private String orderStatus;
    private long daysWaiting;
}
