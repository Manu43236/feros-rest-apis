package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateLrRequest {

    @NotNull(message = "Vehicle allocation is required")
    private Long vehicleAllocationId;

    private LocalDate lrDate;
    @NotNull(message = "Loaded weight is required")
    private BigDecimal loadedWeight;
    private LocalDateTime loadedAt;
    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate ewayBillValidUpto;
    private String remarks;
}