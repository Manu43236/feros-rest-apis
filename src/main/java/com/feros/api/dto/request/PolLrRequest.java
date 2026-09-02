package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PolLrRequest {

    @NotNull(message = "Vehicle is required")
    private Long vehicleId;

    @NotNull(message = "Driver is required")
    private Long driverId;

    private Long cleanerId;

    private String paperLrNumber;

    @NotNull(message = "Vehicle capacity is required")
    private BigDecimal vehicleCapacity;

    @NotNull(message = "Allocated weight is required")
    private BigDecimal allocatedWeight;

    @NotNull(message = "Loaded weight is required")
    private BigDecimal loadedWeight;

    @NotNull(message = "Delivered weight is required")
    private BigDecimal deliveredWeight;

    private LocalDate lrDate;

    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate ewayBillValidUpto;
    private String remarks;
}
