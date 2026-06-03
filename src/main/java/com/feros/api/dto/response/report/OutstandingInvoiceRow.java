package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OutstandingInvoiceRow {
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String clientName;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String invoiceStatus;
    private Long daysOverdue; // null if dueDate not yet passed
}
