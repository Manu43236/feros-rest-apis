package com.feros.api.dto.request;

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

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private LocalDate dueDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;

    private BigDecimal taxPercent;
    private String notes;

    @Valid
    @NotNull(message = "At least one line item is required")
    private List<EquipmentInvoiceItemRequest> items;
}
