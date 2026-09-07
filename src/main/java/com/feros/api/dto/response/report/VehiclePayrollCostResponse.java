package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Builder
public class VehiclePayrollCostResponse {
    private String vehicleNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<VehiclePayrollCostRow> rows;
    private BigDecimal totalAmount;
}
