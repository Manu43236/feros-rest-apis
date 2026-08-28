package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentSessionLogResponse {
    private Long id;
    private Long machineAssignmentId;
    private Long workOrderId;
    private Long equipmentId;
    private Long operatorUserId;
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private BigDecimal startHmr;
    private LocalDateTime endTime;
    private BigDecimal endHmr;
    private BigDecimal fuelConsumed;
    // endHmr - startHmr, null if session still open
    private BigDecimal hmrDelta;
    private Boolean isOpen;
    private String notes;
    private LocalDateTime createdAt;
}
