package com.feros.api.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentServiceTaskRequest {
    private Long taskTypeId;
    private String customName;
    private boolean isRecurring;
    private BigDecimal frequencyHmr;
    private BigDecimal cost;
}
