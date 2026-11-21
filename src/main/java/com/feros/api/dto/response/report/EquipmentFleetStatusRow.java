package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentFleetStatusRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String equipmentType;
    private String make;
    private String model;
    private String workStatus;
    private BigDecimal currentMeterReading;
    private String meterType;
    // Work order it is currently deployed on (when ASSIGNED/BUSY), else null.
    private String workOrderNumber;
    private String clientName;
    private String site;
}
