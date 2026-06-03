package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class InvoiceAgingRow {
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String clientName;
    private BigDecimal balanceDue;
    private long daysOverdue;
    private String agingBucket; // "1-30 days", "31-60 days", "61-90 days", "90+ days"
}
