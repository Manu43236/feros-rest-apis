package com.feros.api.dto.response;

import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.StockTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartsTransactionResponse {
    private Long id;
    private Long sparePartId;
    private String partName;
    private String unit;
    private StockTransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private StockReferenceType referenceType;
    private Long servicePartId;
    private String serviceNumber;
    private String vehicleRegistrationNumber;
    private String supplierName;
    private String notes;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
