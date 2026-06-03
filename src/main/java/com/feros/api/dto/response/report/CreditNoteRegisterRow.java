package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CreditNoteRegisterRow {
    private Long creditNoteId;
    private String creditNoteNumber;
    private LocalDate creditNoteDate;
    private String clientName;
    private String linkedInvoiceNumber; // null if standalone credit note
    private BigDecimal amount;
    private String reason;
    private String creditNoteStatus;
}
