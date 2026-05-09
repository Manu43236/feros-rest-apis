package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationRateResponse {
    private int totalOrders;
    private int cancelled;
    private int active;
    private double cancellationRate;
}
