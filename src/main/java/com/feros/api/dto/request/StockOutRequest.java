package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOutRequest {

    @NotNull(message = "Spare part ID is required")
    private Long sparePartId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // "DAMAGE" | "ADJUSTMENT" — defaults to DAMAGE if not provided
    private String type;

    private String notes;
}
