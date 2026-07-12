package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class EquipmentRetentionReleaseResponse {
    private Long id;
    private Long workOrderId;
    private BigDecimal amount;
    private LocalDate releaseDate;
    private String notes;
    private LocalDateTime createdAt;
}
