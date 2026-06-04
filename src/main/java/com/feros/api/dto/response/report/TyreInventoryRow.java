package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreInventoryRow {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tyreType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private String status; // IN_STOCK / FITTED / RETREADING / SCRAPPED / DISPOSED
    private double totalLifetimeKm;
    private double maxLifetimeKm;
    private double percentLifeUsed;
    private int retreadCount;
    private LocalDate expiryDate;
    // If currently fitted
    private String fittedOnVehicle;
    private String fittedPosition;
}
