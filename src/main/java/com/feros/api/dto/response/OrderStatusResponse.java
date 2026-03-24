package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OrderStatusResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private Long clientId;
    private String clientName;
    private String fromCity;
    private String fromState;
    private String toCity;
    private String toState;
    private String materialType;
    private BigDecimal totalWeight;
    private BigDecimal totalFreightAmount;
    private String orderStatus;
    private String orderPaymentStatus;
}
