package com.feros.api.dto.response;

import com.feros.api.enums.OrderPaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/** Order-level money reconciliation: what was billed vs received vs deducted. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSettlementResponse {
    private Long orderId;
    private String orderNumber;
    private BigDecimal orderValue;        // totalFreightAmount
    private BigDecimal totalAdvance;
    private BigDecimal totalPaid;         // balance payments
    private BigDecimal totalReceived;     // advance + paid (actual cash)
    private BigDecimal totalDeductions;
    private BigDecimal balance;           // orderValue − received − deductions; 0 = squared
    private OrderPaymentStatus paymentStatus;
    private List<OrderPaymentResponse> entries;
}
