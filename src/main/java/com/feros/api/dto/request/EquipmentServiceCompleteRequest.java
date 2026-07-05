package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentServiceCompleteRequest {
    private BigDecimal completedHmr;
    private LocalDate completedDate;
}
