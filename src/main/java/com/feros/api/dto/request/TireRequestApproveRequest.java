package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TireRequestApproveRequest {
    private Long tireId;
    private BigDecimal fittedAtKm;
}
