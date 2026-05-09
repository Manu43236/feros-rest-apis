package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCostBreakdownResponse {
    private Long serviceId;
    private Long vehicleId;
    private String regNo;
    private String vehicleType;
    private LocalDate serviceDate;
    private String serviceType;
    private String status;
    private BigDecimal totalCost;
    private int partsUsedCount;
}
