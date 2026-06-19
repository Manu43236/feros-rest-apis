package com.feros.api.entity;

import com.feros.api.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;

    // Legal & Business Info
    @Column(name = "gstin")
    private String gstin;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "tan_number")
    private String tanNumber;

    @Column(name = "cin_number")
    private String cinNumber;

    @Column(name = "transport_license_number")
    private String transportLicenseNumber;

    // Bank Details
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    // Contact Person
    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_phone")
    private String ownerPhone;

    @Column(name = "owner_email")
    private String ownerEmail;

    // Branding
    @Column(name = "transport_hsn_sac", length = 20)
    private String transportHsnSac;

    @Column(name = "prefix", length = 20)
    private String prefix;

    @Column(name = "logo_url")
    private String logoUrl;

    // Subscription Info
    @Column(name = "lorry_count")
    private Integer lorryCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status")
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    @Column(name = "trial_start_date")
    private LocalDate trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    // User limit override (null = default: vehicleCount × 5)
    @Column(name = "custom_user_limit")
    private Integer customUserLimit;

    // System
    @Column(name = "is_active")
    private Boolean isActive = true;
}