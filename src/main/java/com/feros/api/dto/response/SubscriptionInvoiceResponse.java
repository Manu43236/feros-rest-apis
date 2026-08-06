package com.feros.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long tenantId;
    private String companyName;
    private String planName;
    private String billingCycle;
    private Integer vehicleCount;
    private BigDecimal pricePerVehicle;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal amount;
    private BigDecimal installationCharges;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private String paymentRef;
    private String proformaNumber;
    private String invoiceStatus; // PROFORMA / CONFIRMED
    private String gstType;       // INTRA_STATE / INTER_STATE
    private LocalDate paymentDate;
    private String paymentMode;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    // Tenant details for print/PDF
    private String tenantAddress;
    private String tenantCity;
    private String tenantState;
    private String tenantPincode;
    private String tenantGstin;
    // Additional charges as raw JSON — parsed on frontend for edit pre-fill
    private String additionalChargesJson;
}
