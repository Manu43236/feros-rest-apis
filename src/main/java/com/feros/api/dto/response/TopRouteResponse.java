package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopRouteResponse {
    private String fromCity;
    private String toCity;
    private int orderCount;
    private BigDecimal totalWeight;
}
