package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateInvoiceRequest {
    private LocalDate dueDate;
    private String remarks;
}
