package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DelayedDeliveryRow {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String clientName;
    private String vehicleRegistrationNumber;
    private String driverName;
    private String fromCity;
    private String toCity;
    private String materialType;
    private LocalDateTime loadedAt;
    private long daysInTransit;
    private String lrStatus;
}
