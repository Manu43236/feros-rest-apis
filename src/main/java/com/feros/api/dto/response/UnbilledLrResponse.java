package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UnbilledLrResponse {
    private Long lrId;
    private String lrNumber;
    private String registrationNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private String deliveredAt;
    private BigDecimal deliveredWeight;
    private long daysSinceDelivery;
}
