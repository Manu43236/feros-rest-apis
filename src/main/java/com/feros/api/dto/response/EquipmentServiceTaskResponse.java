package com.feros.api.dto.response;

import com.feros.api.enums.ServiceTaskStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentServiceTaskResponse {
    private Long id;
    private Long taskTypeId;
    private String taskTypeName;
    private String customName;
    private String displayName;
    private boolean isRecurring;
    private BigDecimal frequencyHmr;
    private BigDecimal cost;
    private ServiceTaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
