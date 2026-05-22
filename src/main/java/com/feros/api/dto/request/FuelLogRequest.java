package com.feros.api.dto.request;

import com.feros.api.enums.FuelPaymentMode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FuelLogRequest {

    private Long vehicleId;
    private Long orderId;

    private LocalDateTime fillDate;
    private BigDecimal litresFilled;
    private BigDecimal odometerReading;
    private BigDecimal costPerLitre;
    private BigDecimal totalCost;
    private Boolean isFullTank;
    private FuelPaymentMode paymentMode;
    private String fuelStationName;
    private String fuelStationCity;
    private String receiptUrl;
    private String notes;
}
