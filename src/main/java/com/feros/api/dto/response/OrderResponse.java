package com.feros.api.dto.response;

import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
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

    private Long materialTypeId;
    private String materialTypeName;

    private BigDecimal totalWeight;
    private BigDecimal totalWeightFulfilled;
    private BigDecimal remainingWeight;

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
    private String specialInstructions;
    private String remarks;

    private Long createdById;
    private String createdByName;

    private List<VehicleAllocationResponse> vehicleAllocations;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}