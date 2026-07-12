package com.feros.api.dto.request;

import com.feros.api.enums.GstType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EquipmentInvoiceRequest {

    private Long clientId; // required for client-level invoices (no woId in path)

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private LocalDate dueDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;

    private BigDecimal taxPercent;
    private GstType gstType;
    private BigDecimal retentionPercent;
    private BigDecimal tdsPercent;
    private String notes;

    @Valid
    @NotNull(message = "At least one line item is required")
    private List<EquipmentInvoiceItemRequest> items;
}
