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
    private BigDecimal cgstPercentage; // e.g. 9 for 9% CGST (intra-state)
    private BigDecimal sgstPercentage; // e.g. 9 for 9% SGST (intra-state)
    private BigDecimal igstPercentage; // e.g. 18 for 18% IGST (inter-state)
    private String remarks;
}