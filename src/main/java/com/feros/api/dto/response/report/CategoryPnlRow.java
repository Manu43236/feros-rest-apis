package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

/**
 * P&L aggregated to a vehicle category = (tyre type / wheeler count) × (vehicle body type).
 * Expenses split the way the ops sheet wants them: Diesel, Salaries (lease-aware), Spares,
 * Service, Trip Exp, Others (= tyre purchase + document costs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPnlRow {
    private String tyreType;       // VehicleType name, e.g. "06 TYRE"
    private String vehicleType;    // VehicleBodyType name, e.g. "DCM"
    private Integer tyreCount;     // for sorting
    private int vehicleCount;

    private BigDecimal revenue;

    private BigDecimal diesel;
    private BigDecimal salaries;
    private BigDecimal spares;
    private BigDecimal service;
    private BigDecimal tripExpenses;
    private BigDecimal others;
    private BigDecimal totalExpenses;

    private BigDecimal netPnl;
}
