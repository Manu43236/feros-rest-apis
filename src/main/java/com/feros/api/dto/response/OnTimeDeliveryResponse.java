package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnTimeDeliveryResponse {
    private int totalDelivered;
    private int onTime;
    private int delayed;
    private double onTimeRate;
}
