package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** One row of the machine staff-cost report — operator + cleaner shift pay per machine over the range.
 *  Built shift-aware from snapshots (equipment mirror of the vehicle-cost report, but lease/shift-aware
 *  from day one so it does not repeat the vehicle-cost ≠ By-Role bug). */
@Getter
@Builder
public class MachineStaffCostRow {
    private String machine;          // registration / serial
    private String equipmentType;
    private String workOrderNumber;
    private String clientName;
    private BigDecimal operatorCost;
    private BigDecimal cleanerCost;
    private BigDecimal totalCost;
}
