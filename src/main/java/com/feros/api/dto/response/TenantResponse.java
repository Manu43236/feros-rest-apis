package com.feros.api.dto.response;

import com.feros.api.enums.SubscriptionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private Long id;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String panNumber;
    private String tanNumber;
    private String cinNumber;
    private String transportLicenseNumber;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String accountHolderName;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String prefix;
    private String logoUrl;
    private Integer lorryCount;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate trialStartDate;
    private LocalDate trialEndDate;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Subscription overview (populated for SA views)
    private String currentPlanName;
    private Integer currentVehicleCount;
    private java.math.BigDecimal currentPricePerVehicle;
    private String currentBillingCycle;
    private Integer customUserLimit;
    private Integer effectiveUserLimit;   // customUserLimit ?? vehicleCount × 5
    private Long activeUserCount;
}