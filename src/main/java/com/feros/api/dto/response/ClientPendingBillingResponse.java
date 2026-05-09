package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPendingBillingResponse {
    private Long clientId;
    private String clientName;
    private int pendingLrCount;
    private BigDecimal totalDeliveredTons;
    private LocalDate oldestDeliveryDate;
    private long daysPending;
}
