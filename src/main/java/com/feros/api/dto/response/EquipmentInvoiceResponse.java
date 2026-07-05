package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentInvoiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EquipmentInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long workOrderId;
    private String woNumber;
    private Long clientId;
    private String clientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private EquipmentInvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private List<EquipmentInvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
