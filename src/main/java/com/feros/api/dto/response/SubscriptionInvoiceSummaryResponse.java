package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInvoiceSummaryResponse {
    private BigDecimal totalSubscriptionValue;
    private BigDecimal totalInvoiced;
    private BigDecimal balanceOutstanding;
    private long pendingProformas;
}
