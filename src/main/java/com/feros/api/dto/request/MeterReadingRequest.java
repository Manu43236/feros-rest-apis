package com.feros.api.dto.request;

import com.feros.api.enums.MeterReadingType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MeterReadingRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Reading (km) is required")
    private BigDecimal readingKm;

    @NotNull(message = "Reading type is required")
    private MeterReadingType readingType;

    private Long lrId;
    private String photoUrl;
    private LocalDateTime recordedAt;
    private String notes;
}
