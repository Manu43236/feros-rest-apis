package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TripExpenseReportRow {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String vehicleNumber;
    private String driverName;
    private String cleanerName;
    private String fromCity;
    private String toCity;
    private BigDecimal advanceAmount;
    private BigDecimal driverBatta;
    private BigDecimal cleanerBatta;
    private BigDecimal tripMamulu;
    private BigDecimal itemsTotal;
    private BigDecimal totalExpense;
    private BigDecimal settlementAmount;
    private String status;
}
