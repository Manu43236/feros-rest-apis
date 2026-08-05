package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateProformaInvoiceRequest {

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;

    @NotNull
    @Min(1)
    private Integer vehicleCount;

    @NotNull
    @Positive
    private BigDecimal ratePerVehicle; // per vehicle per month

    @NotNull
    private String gstType; // INTRA_STATE / INTER_STATE

    private String notes;
}
