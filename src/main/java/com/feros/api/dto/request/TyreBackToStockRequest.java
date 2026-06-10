package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TyreBackToStockRequest {
    private BigDecimal retreadingCost;
    private BigDecimal newMaxLifetimeKm;
    private LocalDate actualReturnDate;
    private String notes;
}
