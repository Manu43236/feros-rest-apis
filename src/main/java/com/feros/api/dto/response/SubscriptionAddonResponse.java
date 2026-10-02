package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SubscriptionAddonResponse {
    private Long id;
    private Long tenantId;
    private Long subscriptionHistoryId;
    private Integer addonVehicleCount;
    private BigDecimal pricePerVehicle;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal amount;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private String paymentRef;
    private Long invoiceId;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    // computed convenience fields
    private Integer baseVehicleCount;      // active subscription base count
    private Integer effectiveSlotLimit;    // base + all active add-ons (post-create)
}
