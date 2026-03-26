package com.feros.api.dto.request;

import com.feros.api.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAdvanceRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String referenceNumber;
    private String remarks;
}
