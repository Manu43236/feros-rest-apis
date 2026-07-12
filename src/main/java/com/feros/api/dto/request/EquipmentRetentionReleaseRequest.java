package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentRetentionReleaseRequest {
    private BigDecimal amount;
    private LocalDate releaseDate;
    private String notes;
}
