package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OrderPaymentStatusRow {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private String clientName;
    private BigDecimal totalFreightAmount;
    private String orderStatus;
    private String orderPaymentStatus;
}
