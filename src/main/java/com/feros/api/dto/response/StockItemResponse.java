package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItemResponse {
    private Long inventoryId;
    private Long sparePartId;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private Integer quantity;
    private Integer minStockLevel;
    private Boolean isLowStock; // quantity <= minStockLevel
    private BigDecimal lastUnitCost; // last purchase price
}
