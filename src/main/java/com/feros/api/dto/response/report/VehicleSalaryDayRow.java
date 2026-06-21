package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class VehicleSalaryDayRow {
    private int day;
    private LocalDate date;

    // Driver
    private String driverName;
    private String driverRole;
    private BigDecimal driverDailyRate;
    private BigDecimal driverExtraPay;
    private BigDecimal driverTotal;

    // Cleaner
    private String cleanerName;
    private String cleanerRole;
    private BigDecimal cleanerDailyRate;
    private BigDecimal cleanerExtraPay;
    private BigDecimal cleanerTotal;

    // Day total (null = no attendance that day → show "—")
    private BigDecimal dayTotal;
}
