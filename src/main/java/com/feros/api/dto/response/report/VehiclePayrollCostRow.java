package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Builder
public class VehiclePayrollCostRow {
    private LocalDate date;
    private String vehicleNumber;
    private String staffName;
    private String role;
    private BigDecimal dailyPay;
    private String payrollStatus;
}
