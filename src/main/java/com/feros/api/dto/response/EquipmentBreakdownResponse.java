package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentBreakdownStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentBreakdownResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentIdentifier;
    private LocalDateTime breakdownDate;
    private String location;
    private String reason;
    private String notes;
    private EquipmentBreakdownStatus status;
    private String reportedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private Long workOrderId;
    private String workOrderNumber;
}
