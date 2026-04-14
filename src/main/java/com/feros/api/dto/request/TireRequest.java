package com.feros.api.dto.request;

import com.feros.api.enums.TireType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TireRequest {
    private String serialNumber;
    private String brand;
    private String size;
    private TireType tireType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private String notes;
}
