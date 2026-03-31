package com.feros.api.dto.response;

import com.feros.api.enums.ServiceTaskStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceTaskResponse {
    private Long id;
    private Long taskTypeId;
    private String taskTypeName;
    private String customName;
    private String displayName;
    private Boolean isRecurring;
    private Integer frequencyKm;
    private BigDecimal cost;
    private ServiceTaskStatus status;
}
