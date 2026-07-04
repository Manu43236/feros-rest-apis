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
public class EquipmentMeterReadingResponse {
    private Long id;
    private Long equipmentId;
    private LocalDate readingDate;
    private BigDecimal readingValue;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
