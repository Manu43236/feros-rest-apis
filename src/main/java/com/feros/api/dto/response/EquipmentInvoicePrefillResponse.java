package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentInvoicePrefillResponse {
    private Long machineAssignmentId;
    private Long equipmentId;
    private Long workOrderId;
    private String woNumber;
    private String serialNumber;
    private String equipmentTypeName;
    private BigDecimal suggestedHours;   // sum of hoursWorked in period
    private Integer suggestedDays;       // count of working log dates in period
    private BigDecimal suggestedMonths;  // computed from period length
    private BigDecimal woRate;           // rate from work order
    private String woRateType;           // HOURLY / DAILY_SHIFT
}
