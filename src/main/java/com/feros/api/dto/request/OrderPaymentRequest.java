package com.feros.api.dto.request;

import com.feros.api.enums.DeductionReason;
import com.feros.api.enums.OrderPaymentType;
import com.feros.api.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OrderPaymentRequest {

    @NotNull(message = "Entry type is required")
    private OrderPaymentType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate txnDate;

    private PaymentMode paymentMode;          // for ADVANCE / PAYMENT
    private DeductionReason deductionReason;  // for DEDUCTION
    private String remarks;
}
