package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditNoteSummaryResponse {
    private Long creditNoteId;
    private String creditNoteNumber;
    private String clientName;
    private LocalDate creditNoteDate;
    private BigDecimal amount;
    private String reason;
    private String status;
    private String invoiceNumber;
}
