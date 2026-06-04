package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockOutwardRow {
    private Long transactionId;
    private LocalDateTime transactionDate;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private int quantity;
    private BigDecimal totalCost;
    private String transactionType; // OUT / DAMAGE
    private String vehicleRegistration;
    private String requestedBy;
    private String approvedBy;
    private String notes;
}
