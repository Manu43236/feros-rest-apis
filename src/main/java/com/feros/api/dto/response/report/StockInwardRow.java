package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockInwardRow {
    private Long transactionId;
    private LocalDateTime transactionDate;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String supplierName;
    private String referenceType; // PURCHASE / ADJUSTMENT
    private String receivedBy;
    private String notes;
}
