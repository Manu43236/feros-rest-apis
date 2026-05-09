package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopMaterialResponse {
    private String materialType;
    private int orderCount;
    private BigDecimal totalWeight;
    private double pct;
}
