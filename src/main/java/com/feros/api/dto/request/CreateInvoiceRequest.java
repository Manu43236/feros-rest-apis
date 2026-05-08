package com.feros.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateInvoiceRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    @NotEmpty(message = "At least one LR is required")
    private List<Long> lrIds;

    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal cgstPercentage; // e.g. 9 for 9% CGST
    private BigDecimal sgstPercentage; // e.g. 9 for 9% SGST (use 0 + igstPercentage for interstate)
    private String remarks;
}