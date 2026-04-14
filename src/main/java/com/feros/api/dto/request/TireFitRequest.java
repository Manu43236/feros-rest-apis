package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TireFitRequest {
    private Long vehicleId;
    private Long tireId;
    private Long positionId;
    private BigDecimal fittedAtKm;
    private LocalDate fittedDate;
    private String notes;
}
