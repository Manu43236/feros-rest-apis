package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EquipmentMaintenanceRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String equipmentType;
    private String serviceNumber;
    private LocalDate serviceDate;
    private LocalDate completedDate;
    private String serviceType;
    private String triggeredBy;
    private String reason;
    private Integer taskCount;
    private BigDecimal totalCost;
    private String status;
    private String vendorName;
    private BigDecimal hmrAtService;
    private BigDecimal dueAtHmr;
    private BigDecimal downtimeHours;
}
