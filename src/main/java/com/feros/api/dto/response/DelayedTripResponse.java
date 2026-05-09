package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DelayedTripResponse {
    private Long lrId;
    private String lrNumber;
    private String registrationNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private LocalDate expectedDeliveryDate;
    private long daysDelayed;
    private String loadedAt;
}
