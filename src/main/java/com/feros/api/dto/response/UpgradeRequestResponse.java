package com.feros.api.dto.response;

import com.feros.api.enums.BillingCycle;
import com.feros.api.enums.UpgradeRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UpgradeRequestResponse {
    private Long id;
    private Long tenantId;
    private String companyName;
    private Long planId;
    private String planName;
    private BigDecimal pricePerVehicle;
    private Integer vehicleCount;
    private BillingCycle billingCycle;
    private BigDecimal estimatedBase;   // before GST
    private BigDecimal estimatedTotal;  // including GST
    private String notes;
    private UpgradeRequestStatus status;
    private LocalDateTime createdAt;
}
