package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLrResponse {
    private Long id;
    private Long lrId;
    private String lrNumber;
    private String paperLrNumber;
    private LocalDate lrDate;
    private Long orderId;
    private String orderNumber;
    private String vehicleRegistrationNumber;
    private String materialTypeName;
    private String unitName; // unit of measure for billingWeight (order's unit; "Tons" default)
    private BigDecimal billingWeight; // quantity used for billing, in unitName
    private String freightRateType; // PER_TON / PER_TRIP / PER_KM
    private BigDecimal freightRate; // rate per unit
    private BigDecimal freightAmount;
    private BigDecimal chargesAmount;
    private BigDecimal checkpostFineAmount;
    private BigDecimal totalAmount;
    private String remarks;
    private LocalDateTime createdAt;
}