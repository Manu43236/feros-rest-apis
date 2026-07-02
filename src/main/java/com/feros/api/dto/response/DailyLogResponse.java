package com.feros.api.dto.response;

import com.feros.api.enums.DailyLogStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DailyLogResponse {
    private Long id;
    private Long machineAssignmentId;
    private Long workOrderId;
    private LocalDate logDate;
    private DailyLogStatus status;
    private BigDecimal startHourMeter;
    private BigDecimal endHourMeter;
    private BigDecimal hoursWorked;
    private BigDecimal fuelConsumed;
    private String notes;
    // denormalized for display
    private String serialNumber;
    private String equipmentTypeName;
    private String divisionName;
    private String source; // MANUAL or AUTO
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
