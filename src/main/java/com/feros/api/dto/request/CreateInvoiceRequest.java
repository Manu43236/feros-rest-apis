package com.feros.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
    private String remarks;
}