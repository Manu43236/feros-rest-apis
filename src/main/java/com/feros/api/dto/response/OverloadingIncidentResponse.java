package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OverloadingIncidentResponse {
    private Long lrId;
    private String lrNumber;
    private String registrationNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private LocalDate lrDate;
    private BigDecimal allocatedWeight;
    private BigDecimal loadedWeight;
    private BigDecimal overloadWeight;
}
