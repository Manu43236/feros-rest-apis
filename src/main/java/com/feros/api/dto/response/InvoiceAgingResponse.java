package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAgingResponse {
    private int totalOutstanding;
    private BigDecimal totalAmount;
    private AgingBucket bucket0to30;
    private AgingBucket bucket31to60;
    private AgingBucket bucket61to90;
    private AgingBucket bucket90plus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingBucket {
        private int count;
        private BigDecimal amount;
        private List<AgingRow> invoices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingRow {
        private Long invoiceId;
        private String invoiceNumber;
        private String clientName;
        private LocalDate invoiceDate;
        private BigDecimal balanceDue;
        private int ageInDays;
    }
}
