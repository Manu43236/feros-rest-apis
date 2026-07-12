package com.feros.api.dto.response;

import com.feros.api.enums.PaymentMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class EquipmentPaymentResponse {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private Long workOrderId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMode paymentMode;
    private String utrReference;
    private String notes;
    private LocalDateTime createdAt;
}
