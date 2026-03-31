package com.feros.api.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceTaskRequest {
    private Long taskTypeId;
    private String customName;
    @Builder.Default
    private Boolean isRecurring = false;
    private Integer frequencyKm;
    private BigDecimal cost;
}
