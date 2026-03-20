package com.feros.api.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private Integer maxLorries;
    private Integer maxUsers;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private String features;
    private Boolean isActive;
}
