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
public class InvoicePaymentResponse {
    private Long id;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private String referenceNumber;
    private String remarks;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}