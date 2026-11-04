package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TyreRequestApproveRequest {
    private BigDecimal fittedAtKm;
    private Long tyreId;   // store keeper's chosen tyre; falls back to oldest IN_STOCK (FIFO) when null
}
