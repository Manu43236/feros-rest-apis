package com.feros.api.dto.request;

import com.feros.api.enums.FuelPaymentMode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentFuelLogRequest {
    private LocalDate fillDate;
    private BigDecimal litresFilled;
    private BigDecimal hmrAtFill;
    private BigDecimal costPerLitre;
    private BigDecimal totalCost;
    private Boolean isFullTank;
    private FuelPaymentMode paymentMode;
    private String fuelStation;
    private String notes;
}
