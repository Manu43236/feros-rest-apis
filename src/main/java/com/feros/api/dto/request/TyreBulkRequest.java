package com.feros.api.dto.request;

import com.feros.api.enums.TyrePurchaseCondition;
import com.feros.api.enums.TyreType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TyreBulkRequest {
    // Serial numbers — one tyre created per entry
    private List<String> serialNumbers;

    // Common specs shared across all tyres in the batch
    private String brand;
    private String size;
    private TyreType tyreType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private Integer tyreLifeYears;
    private BigDecimal maxLifetimeKm;
    private TyrePurchaseCondition purchaseCondition;
    private BigDecimal kmAtPurchase;
    private Integer retreadCountAtPurchase;
    private String notes;

    // Purchase reference — common to the batch
    private String supplierName;
    private String invoiceNumber;
}
