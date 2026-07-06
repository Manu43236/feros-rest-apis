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
    private Integer vehicleCount;
    private BigDecimal pricePerVehicle;
    private Integer maxLorries;
    private Integer maxUsers;
    // Feature flags — always true (all features included for all tenants)
    private Boolean hasFuelLogs;
    private Boolean hasMeterReadings;
    private Boolean hasVehicleServices;
    private Boolean hasAttendance;
    private Boolean hasPayroll;
    private Boolean hasInventory;
    private Boolean hasReports;
    private Boolean hasCreditNotes;
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
