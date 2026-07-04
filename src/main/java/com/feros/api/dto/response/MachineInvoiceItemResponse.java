package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentBillingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MachineInvoiceItemResponse {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String invoiceStatus;
    private String clientName;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private String description;
    private EquipmentBillingType billingType;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
}
