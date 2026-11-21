package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class WorkOrderReceivableRow {
    private Long woId;
    private String woNumber;
    private String clientName;
    private Integer invoiceCount;
    private BigDecimal grossBilled;
    private BigDecimal totalReceived;
    private BigDecimal retentionHeld;
    private BigDecimal totalAdvances;
    private BigDecimal balanceDue;
    private LocalDate oldestUnpaidDueDate;
    private Long daysOverdue;
    private String agingBucket; // CURRENT | 0-30 | 31-60 | 61-90 | 90+
}
