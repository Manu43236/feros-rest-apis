package com.feros.api.dto.request;

import com.feros.api.enums.TyreRemovalReason;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TyreRemoveRequest {
    private BigDecimal removedAtKm;
    private LocalDate removedDate;
    private TyreRemovalReason removalReason;
    private String notes;
    private String retreaderName;
    private LocalDate expectedReturnDate;
}
