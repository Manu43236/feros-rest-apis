package com.feros.api.dto.response;

import com.feros.api.enums.PaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAdvanceResponse {
    private Long id;
    private Long tenantId;
    private Long clientId;
    private String clientName;
    private LocalDate receivedDate;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private String referenceNumber;
    private Boolean isAdjusted;
    private BigDecimal adjustedAmount;
    private BigDecimal pendingAmount;
    private Long adjustedInvoiceId;
    private String adjustedInvoiceNumber;
    private LocalDateTime adjustedAt;
    private String remarks;
    private Long createdById;
    private String createdByName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
