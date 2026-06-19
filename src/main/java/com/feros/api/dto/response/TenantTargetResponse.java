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
    private Integer completedTrips;
    private Integer pendingTrips;
    private Integer localTrips;
    private Integer nonLocalTrips;

    private BigDecimal completedTons;
    private BigDecimal pendingTons;
    private BigDecimal localTons;
    private BigDecimal nonLocalTons;

    // Progress percentages
    private Double tripsProgressPct;
    private Double tonsProgressPct;
}
