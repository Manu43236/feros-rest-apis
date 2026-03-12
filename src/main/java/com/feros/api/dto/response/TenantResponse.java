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
}