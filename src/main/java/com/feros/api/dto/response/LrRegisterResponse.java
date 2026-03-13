package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LrRegisterResponse {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String orderNumber;
    private Long clientId;
    private String clientName;
    private String vehicleRegistrationNumber;
    private String fromCity;
    private String fromState;
    private String toCity;
    private String toState;
    private String materialType;
    private BigDecimal allocatedWeight;
    private BigDecimal loadedWeight;
    private BigDecimal deliveredWeight;
    private BigDecimal weightVariance;
    private Boolean isOverloaded;
    private LocalDateTime loadedAt;
    private LocalDateTime deliveredAt;
    private String freightRateType;
    private BigDecimal freightRate;
    private String lrStatus;
}
