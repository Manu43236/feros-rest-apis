package com.feros.api.dto.response;

import com.feros.api.enums.CreditNoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNoteResponse {
    private Long id;
    private Long tenantId;
    private String creditNoteNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
    private LocalDate creditNoteDate;
    private BigDecimal amount;
    private String reason;
    private CreditNoteStatus creditNoteStatus;
    private String remarks;
    private Long createdById;
    private String createdByName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
