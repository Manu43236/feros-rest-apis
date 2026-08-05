package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private BigDecimal ratePerVehicle;

    @NotNull
    private String gstType; // INTRA_STATE / INTER_STATE

    private List<AdditionalCharge> additionalCharges;

    private String notes;

    @Getter
    @Setter
    public static class AdditionalCharge {
        private String name;
        private BigDecimal amount;
    }
}
