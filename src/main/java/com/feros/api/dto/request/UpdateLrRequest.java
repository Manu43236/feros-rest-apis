package com.feros.api.dto.request;

import com.feros.api.enums.LrStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateLrRequest {
    private BigDecimal loadedWeight;
    private LocalDateTime loadedAt;
    private BigDecimal deliveredWeight;
    private LocalDateTime deliveredAt;
    private LrStatus lrStatus;
    private String remarks;

    // Odometer readings (required when lrStatus = IN_TRANSIT or DELIVERED)
    private BigDecimal startOdometer;
    private String startOdometerPhotoUrl;
    private BigDecimal endOdometer;
    private String endOdometerPhotoUrl;
}