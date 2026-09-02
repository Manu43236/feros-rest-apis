package com.feros.api.dto.request;

import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PostOrderLogRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    private Long materialTypeId;
    private String customMaterialName;

    @NotNull(message = "Total weight is required")
    private BigDecimal totalWeight;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

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

    @NotEmpty(message = "At least one LR is required")
    @Valid
    private List<PolLrRequest> lrs;
}
