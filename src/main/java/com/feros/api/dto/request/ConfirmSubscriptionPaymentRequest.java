package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ConfirmSubscriptionPaymentRequest {

    @NotNull
    @Positive
    private BigDecimal receivedAmount; // actual total received incl. GST

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private String paymentMode; // NEFT / RTGS / UPI / CHEQUE / CASH

    private String paymentRef;
}
