package com.feros.api.dto.response.report;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockSummaryRow {
    private Long partId;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private int quantityOnHand;
    private int minStockLevel;
    private String stockStatus; // OK / LOW / OUT
}
