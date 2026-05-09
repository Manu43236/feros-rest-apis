package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long transactionId;
    private LocalDate date;
    private Long partId;
    private String partName;
    private String partNumber;
    private String category;
    private String transactionType;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String supplierName;
    private String notes;
}
