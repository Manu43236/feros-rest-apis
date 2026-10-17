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

    // Party details (optional). clientId = placing client; billingPartyClientId = who is
    // billed (null → bill the placing client).
    private Long consignorClientId;
    private Long consigneeClientId;
    private Long billingPartyClientId;

    private Long materialTypeId;
    private String customMaterialName;

    @NotNull(message = "Total weight is required")
    private BigDecimal totalWeight;

    // Unit of measure for totalWeight (single-material). Null → Tons.
    private Long unitId;

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

    // Multi-material (cargo manifest) — same semantics as OrderRequest.
    private Boolean isMultiMaterial;

    @Valid
    private List<OrderItemRequest> items;

    @NotEmpty(message = "At least one LR is required")
    @Valid
    private List<PolLrRequest> lrs;
}
