package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EquipmentBreakdownRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String equipmentType;
    private LocalDate breakdownDate;
    private String location;
    private String reason;
    private String status;
    private Long daysLost;
    private String reportedBy;
    private String workOrderNumber;
}
