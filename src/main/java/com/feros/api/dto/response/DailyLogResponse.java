package com.feros.api.dto.response;

import com.feros.api.enums.DailyLogStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DailyLogResponse {
    private Long id;
    private Long machineAssignmentId;
    private Long workOrderId;
    private LocalDate logDate;
    private DailyLogStatus status;
    // Aggregates computed from division lines
    private BigDecimal startHourMeter;
    private BigDecimal endHourMeter;
    private BigDecimal hoursWorked;
    private BigDecimal fuelConsumed;
    private String notes;
    // denormalized for display
    private String serialNumber;
    private String equipmentTypeName;
    private String source; // MANUAL or AUTO
    private String woNumber; // populated for machine-scoped log queries
    private List<DailyLogDivisionResponse> divisions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
