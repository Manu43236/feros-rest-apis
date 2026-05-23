package com.feros.api.dto.response;

import com.feros.api.enums.TyreStatus;
import com.feros.api.enums.TyreType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TyreResponse {
    private Long id;
    private Long tenantId;
    private String serialNumber;
    private String brand;
    private String size;
    private TyreType tyreType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private TyreStatus status;
    private Integer retreadCount;
    private BigDecimal totalLifetimeKm;
    private String notes;
    private Integer tyreLifeYears;
    private LocalDate expiryDate;
    private BigDecimal maxLifetimeKm;
    // Retread info (populated when status = RETREADING)
    private String retreaderName;
    private LocalDate expectedReturnDate;
    // Current fitting info (populated when status = FITTED)
    private Long currentFittingId;
    private String currentVehicleRegistrationNumber;
    private String currentPositionCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
