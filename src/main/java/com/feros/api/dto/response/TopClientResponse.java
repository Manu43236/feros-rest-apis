package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopClientResponse {
    private Long clientId;
    private String clientName;
    private int orderCount;
    private int tripCount;
    private BigDecimal totalTonnage;
    private BigDecimal totalRevenue;
}
