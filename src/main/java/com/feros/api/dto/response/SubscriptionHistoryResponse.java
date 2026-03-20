package com.feros.api.dto.response;

import com.feros.api.enums.BillingCycle;
import com.feros.api.enums.SubscriptionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionHistoryResponse {
    private Long id;
    private Long tenantId;
    private String companyName;
    private String planName;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private String paymentRef;
    private String notes;
    private LocalDateTime createdAt;
}
