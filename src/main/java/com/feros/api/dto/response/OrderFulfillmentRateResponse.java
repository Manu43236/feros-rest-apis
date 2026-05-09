package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderFulfillmentRateResponse {
    private int totalOrders;
    private int pending;
    private int partiallyAssigned;
    private int fullyAssigned;
    private int inTransit;
    private int delivered;
    private int completed;
    private int cancelled;
    private double fulfillmentRate;
}
