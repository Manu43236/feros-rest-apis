package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OrderRegisterRow {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String clientName;
    private String materialType;
    private String fromCity;
    private String fromState;
    private String toCity;
    private String toState;
    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private String freightRateType;
    private BigDecimal freightRate;
    private BigDecimal totalFreightAmount;
    private String orderStatus;
    private String orderPaymentStatus;
    private String invoiceStatus;       // INVOICED / NOT_INVOICED
    private BigDecimal advanceReceived;  // advance received from party
    private BigDecimal paidAmount;       // payments received (excludes advance) = received − advance
    private BigDecimal balance;          // order value − received − deductions
    private long vehicleCount;
    private Integer durationDays;
}
