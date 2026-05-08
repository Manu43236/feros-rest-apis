package com.feros.api.dto.response;

import com.feros.api.enums.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Long id;
    private Long tenantId;
    private String invoiceNumber;

    private Long clientId;
    private String clientName;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    private BigDecimal subtotal;
    private BigDecimal cgstPercentage;
    private BigDecimal sgstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal advanceAdjusted;
    private BigDecimal creditNoteAdjusted;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;

    private InvoiceStatus invoiceStatus;
    private String remarks;

    // Tenant details (for print)
    private String tenantLogoUrl;
    private String tenantGstin;
    private String tenantPan;
    private String tenantAddress;
    private String tenantCity;
    private String tenantState;
    private String tenantPincode;
    private String tenantBankName;
    private String tenantAccountNumber;
    private String tenantIfscCode;
    private String tenantBranchName;
    private String tenantAccountHolderName;
    private String transportHsnSac;

    // Client details (for print)
    private String clientGstin;
    private String clientAddress;
    private String clientCity;
    private String clientState;
    private String clientPincode;

    private List<InvoiceLrResponse> lrItems;
    private List<InvoicePaymentResponse> payments;

    private Long createdById;
    private String createdByName;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}