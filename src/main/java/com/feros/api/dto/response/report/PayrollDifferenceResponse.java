package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reconciles Payroll by Role (gross) with Vehicle Cost. By construction:
 * totalGross = totalVehicleCost + totalDifference, and the difference breaks down into
 * trip bonus + overtime + other (standby / un-attributed days). {@code rows} lists only the
 * employees with a non-zero difference.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollDifferenceResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalGross;
    private BigDecimal totalVehicleCost;
    private BigDecimal totalDifference;
    private BigDecimal totalTripBonus;
    private BigDecimal totalOvertime;
    private BigDecimal totalOther;
    private List<PayrollDifferenceRow> rows;
}
