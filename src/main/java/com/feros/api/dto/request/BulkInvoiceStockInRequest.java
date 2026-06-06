package com.feros.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkInvoiceStockInRequest {

    private String supplierName;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private LocalDate receivedDate; // stock entry date — allows backdating
    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<LineItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItem {

        @NotNull(message = "Spare part ID is required")
        private Long sparePartId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private BigDecimal unitCost;
        private String itemNotes;
    }
}
