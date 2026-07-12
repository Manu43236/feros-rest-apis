package com.feros.api.dto.request;

import com.feros.api.enums.PaymentMode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentAdvanceRequest {
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMode paymentMode;
    private String utrReference;
    private String notes;
}
