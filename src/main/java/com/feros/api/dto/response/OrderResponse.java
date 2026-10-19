package com.feros.api.dto.response;

import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long tenantId;
    private String orderNumber;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;

    private Long clientId;
    private String clientName;

    // Party details. clientId = placing client.
    private Long consignorClientId;
    private String consignorClientName;
    private Long consigneeClientId;
    private String consigneeClientName;
    // Billing party (who is invoiced). Falls back to clientId when not explicitly set.
    private Long billingPartyClientId;
    private String billingPartyClientName;

    private Long materialTypeId;
    private String materialTypeName;

    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private BigDecimal remainingWeight;

    // Unit of measure. unitId null when using the default; unitName always a label ("Tons" default).
    private Long unitId;
    private String unitName;

    private String sourceAddress;
    private Long sourceCityId;
    private String sourceCityName;
    private Long sourceStateId;
    private String sourceStateName;

    private String destinationAddress;
    private Long destinationCityId;
    private String destinationCityName;
    private Long destinationStateId;
    private String destinationStateName;

    private Long routeId;
    private String routeName;

    private FreightRateType freightRateType;
    private BigDecimal freightRate;
    private BillingOn billingOn;
    private BigDecimal totalFreightAmount;

    private OrderStatus orderStatus;
    private OrderPaymentStatus orderPaymentStatus;
    private String specialInstructions;
    private String remarks;

    private Long createdById;
    private String createdByName;

    private List<VehicleAllocationResponse> vehicleAllocations;
    private AllocationSummaryResponse allocationSummary;

    private Boolean isPol;
    private Boolean isMultiMaterial;
    private List<OrderItemResponse> items;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}