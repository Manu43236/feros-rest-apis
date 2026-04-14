package com.feros.api.dto.request;

import com.feros.api.enums.TireRemovalReason;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TireRemoveRequest {
    private BigDecimal removedAtKm;
    private LocalDate removedDate;
    private TireRemovalReason removalReason;
    private String notes;
}
