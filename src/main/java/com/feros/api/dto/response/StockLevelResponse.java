package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelResponse {
    private Long partId;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private Integer currentStock;
    private Integer minStockLevel;
    private boolean isLowStock;
}
