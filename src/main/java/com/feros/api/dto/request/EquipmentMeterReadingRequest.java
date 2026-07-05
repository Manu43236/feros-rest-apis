package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EquipmentMeterReadingRequest {
    private LocalDateTime readingDate;
    private BigDecimal readingValue;
    private String notes;
}
