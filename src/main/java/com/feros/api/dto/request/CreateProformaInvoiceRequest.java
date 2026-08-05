package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProformaInvoiceRequest {

    @NotNull
    private Long historyId;

    @NotNull
    @Positive
    private BigDecimal expectedAmount; // total incl. GST

    @NotNull
    private String gstType; // INTRA_STATE / INTER_STATE

    private String notes;
}
