package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AddVehicleAddonRequest {

    @NotNull
    @Min(1)
    private Integer vehicleCount;        // extra slots to add

    private BigDecimal pricePerVehicle;  // optional — defaults to active subscription rate

    private String paymentRef;           // optional

    private String notes;                // optional
}
