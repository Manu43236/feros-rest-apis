package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CollectionReportResponse {
    private Long paymentId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNumber;
    private String remarks;
    private Long invoiceId;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
}
