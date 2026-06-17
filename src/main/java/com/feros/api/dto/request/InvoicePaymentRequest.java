package com.feros.api.dto.request;

import com.feros.api.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class InvoicePaymentRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String paymentModeLabel;

    private LocalDate paymentDate;
    private String referenceNumber;
    private String remarks;
}