package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder
public class WoReceivablesSummaryResponse {
    private BigDecimal grossBilled;
    private BigDecimal totalReceived;
    private BigDecimal retentionHeld;
    private BigDecimal totalRetentionReleased;
    private BigDecimal totalAdvances;
    private BigDecimal balanceDue;
    private List<InvoiceReceivableRow> invoices;
    private List<EquipmentAdvanceResponse> advances;
    private List<EquipmentRetentionReleaseResponse> retentionReleases;

    @Getter @Builder
    public static class InvoiceReceivableRow {
        private Long invoiceId;
        private String invoiceNumber;
        private String invoiceDate;
        private BigDecimal totalAmount;
        private BigDecimal retentionOnInvoice;
        private BigDecimal totalReceived;
        private BigDecimal balanceDue;
        private String status;
        private List<EquipmentPaymentResponse> payments;
    }
}
