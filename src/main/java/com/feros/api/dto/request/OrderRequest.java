package com.feros.api.dto.request;

import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    // Either materialTypeId (existing master) or customMaterialName (free-text "Other") must be provided
    private Long materialTypeId;

    private String customMaterialName;

    @NotNull(message = "Total weight is required")
    private BigDecimal totalWeight;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;

    private String sourceAddress;

    @NotNull(message = "Source city is required")
    private Long sourceCityId;

    @NotNull(message = "Source state is required")
    private Long sourceStateId;

    private String destinationAddress;

    @NotNull(message = "Destination city is required")
    private Long destinationCityId;

    @NotNull(message = "Destination state is required")
    private Long destinationStateId;

    private Long routeId;

    @NotNull(message = "Freight rate type is required")
    private FreightRateType freightRateType;

    @NotNull(message = "Freight rate is required")
    private BigDecimal freightRate;

    private BillingOn billingOn;
    private String specialInstructions;
    private String remarks;
    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate ewayBillValidUpto;
}