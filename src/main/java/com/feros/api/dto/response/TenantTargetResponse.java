package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TenantTargetResponse {
    private Long id;
    private Integer year;
    private Integer month;
    private Integer targetTrips;
    private BigDecimal targetTons;

    // Actuals
    private Integer actualTrips;
    private BigDecimal actualTons;

    // Progress percentages
    private Double tripsProgressPct;
    private Double tonsProgressPct;
}
