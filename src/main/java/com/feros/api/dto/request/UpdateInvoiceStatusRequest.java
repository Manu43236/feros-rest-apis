package com.feros.api.dto.request;

import com.feros.api.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInvoiceStatusRequest {
    @NotNull(message = "Status is required")
    private InvoiceStatus invoiceStatus;
    private String remarks;
}