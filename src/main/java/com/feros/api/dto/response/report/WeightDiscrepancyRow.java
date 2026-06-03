package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class WeightDiscrepancyRow {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String clientName;
    private String vehicleRegistrationNumber;
    private String fromCity;
    private String toCity;
    private String materialType;
    private BigDecimal allocatedWeight;
    private BigDecimal loadedWeight;
    private BigDecimal deliveredWeight;
    private BigDecimal weightVariance;
    private Boolean isOverloaded;
    private String lrStatus;
}
