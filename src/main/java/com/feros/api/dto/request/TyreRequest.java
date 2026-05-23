package com.feros.api.dto.request;

import com.feros.api.enums.TyreType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TyreRequest {
    private String serialNumber;
    private String brand;
    private String size;
    private TyreType tyreType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private String notes;
    private Integer tyreLifeYears;
    private BigDecimal maxLifetimeKm;
}
