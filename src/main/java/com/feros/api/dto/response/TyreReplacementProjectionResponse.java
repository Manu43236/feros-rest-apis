package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyreReplacementProjectionResponse {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private Long vehicleId;
    private String vehicleRegNo;
    private String positionCode;
    private LocalDate fittedDate;
    private BigDecimal totalLifetimeKm;
    private BigDecimal maxLifetimeKm;
    private BigDecimal remainingKm;
    private String urgency; // HIGH / MEDIUM / LOW
}
