package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LrRegisterRow {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String orderNumber;
    private String clientName;
    private String vehicleRegistrationNumber;
    private String driverName;
    private String cleanerName;
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
    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate ewayBillValidUpto;
    private String lrStatus;
    private String remarks;
}
