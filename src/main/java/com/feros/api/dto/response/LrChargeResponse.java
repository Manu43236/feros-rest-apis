package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrChargeResponse {
    private Long id;
    private Long chargeTypeId;
    private String chargeTypeName;
    private BigDecimal amount;
    private String remarks;
    private LocalDateTime createdAt;
}