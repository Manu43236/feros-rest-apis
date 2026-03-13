package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SalaryAdvanceRequest {

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private LocalDate advanceDate;
    private String reason;
    private String remarks;
}