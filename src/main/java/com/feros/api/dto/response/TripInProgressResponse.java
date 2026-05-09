package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TripInProgressResponse {
    private Long lrId;
    private String lrNumber;
    private String registrationNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private String loadedAt;
    private LocalDate expectedDeliveryDate;
    private long daysInTransit;
    private BigDecimal loadedWeight;
}
