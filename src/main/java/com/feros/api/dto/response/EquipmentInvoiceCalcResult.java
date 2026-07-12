package com.feros.api.dto.response;

import com.feros.api.enums.RateType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentInvoiceCalcResult {

    private Long machineAssignmentId;
    private String serialNumber;
    private String equipmentTypeName;
    private String woNumber;
    private RateType rateType;

    // Billing state hours breakdown
    private BigDecimal workingHours;
    private BigDecimal standbyHours;
    private BigDecimal idleClientHours;
    private BigDecimal guaranteedHours;
    private BigDecimal billedBaseHours;   // max(working+idleClient, guaranteed)
    private BigDecimal otHours;           // working hours above guaranteed

    // Billing state days breakdown (DAILY_SHIFT)
    private Integer workingDays;
    private Integer standbyDays;

    // Computed amounts
    private BigDecimal baseRate;
    private BigDecimal baseAmount;
    private BigDecimal otRate;
    private BigDecimal otAmount;
    private BigDecimal standbyRate;
    private BigDecimal standbyAmount;
    private BigDecimal totalAmount;
}
