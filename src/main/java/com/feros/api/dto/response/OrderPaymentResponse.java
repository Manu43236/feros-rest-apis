package com.feros.api.dto.response;

import com.feros.api.enums.DeductionReason;
import com.feros.api.enums.OrderPaymentType;
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
public class OrderPaymentResponse {
    private Long id;
    private OrderPaymentType type;
    private BigDecimal amount;
    private LocalDate txnDate;
    private PaymentMode paymentMode;
    private DeductionReason deductionReason;
    private String remarks;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
