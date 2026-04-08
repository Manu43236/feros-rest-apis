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
public class FuelLogResponse {

    private Long id;
    private Long tenantId;

    private Long vehicleId;
    private String vehicleRegistrationNumber;

    private Long orderId;
    private String orderNumber;

    private Long filledById;
    private String filledByName;

    private LocalDate fillDate;
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

    // Calculated
    private BigDecimal mileageKmPerLitre;
    private BigDecimal kmTravelled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
