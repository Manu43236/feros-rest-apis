package com.feros.api.dto.response;

import com.feros.api.enums.TireStatus;
import com.feros.api.enums.TireType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TireResponse {
    private Long id;
    private Long tenantId;
    private String serialNumber;
    private String brand;
    private String size;
    private TireType tireType;
    private String plyRating;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private TireStatus status;
    private Integer retreadCount;
    private BigDecimal totalLifetimeKm;
    private String notes;
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
