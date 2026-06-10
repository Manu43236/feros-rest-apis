package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TyreRetreadLogResponse {
    private Long id;
    private Long tyreId;
    private String tyreSerialNumber;
    private Integer retreadNumber;
    private LocalDate sentDate;
    private LocalDate returnDate;
    private String retreaderName;
    private BigDecimal kmAtSend;
    private BigDecimal retreadingCost;
    private BigDecimal newMaxLifetimeKm;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
