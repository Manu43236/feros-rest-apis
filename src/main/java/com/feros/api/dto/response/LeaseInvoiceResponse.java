package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentInvoiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class LeaseInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long leaseId;
    private String leaseNumber;
    private Long clientId;
    private String clientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private EquipmentInvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal cgstPercentage;
    private BigDecimal sgstPercentage;
    private BigDecimal igstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private List<LeaseInvoiceItemResponse> items;
    // for print page
    private String tenantName;
    private String tenantGstin;
    private String tenantAddress;
    private String tenantState;
    private String clientGstin;
    private String clientAddress;
    private String clientStateName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
