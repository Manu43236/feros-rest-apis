package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TyreRequestApproveRequest {
    private BigDecimal fittedAtKm;
}
