package com.feros.api.dto.response;

import com.feros.api.enums.FuelPaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentFuelLogResponse {
    private Long id;
    private Long equipmentId;
    private LocalDate fillDate;
    private BigDecimal litresFilled;
    private BigDecimal hmrAtFill;
    private BigDecimal costPerLitre;
    private BigDecimal totalCost;
    private Boolean isFullTank;
    private FuelPaymentMode paymentMode;
    private String fuelStation;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
