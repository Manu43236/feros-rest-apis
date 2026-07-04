package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentDashboardResponse {

    // Machine counts by work status
    private long totalMachines;
    private long available;
    private long assigned;
    private long busy;
    private long inRepair;
    private long breakdown;

    // Work orders
    private long activeWorkOrders;
    private long totalWorkOrders;

    // Hours
    private BigDecimal hoursToday;
    private BigDecimal hoursThisMonth;
}
