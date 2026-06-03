package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CollectionRow {
    private Long paymentId;
    private LocalDate paymentDate;
    private String invoiceNumber;
    private String clientName;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNumber;
    private String remarks;
}
