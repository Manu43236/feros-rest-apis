package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WeightVarianceReportResponse {
    private Long clientId;
    private String clientName;
    private int lrCount;
    private BigDecimal totalLoadedWeight;
    private BigDecimal totalDeliveredWeight;
    private BigDecimal totalVariance;
    private double avgVariancePct;
}
