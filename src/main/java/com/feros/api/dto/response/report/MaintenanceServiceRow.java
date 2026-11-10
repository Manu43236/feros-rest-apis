package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MaintenanceServiceRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String serviceNumber;
    private LocalDate serviceDate;
    private LocalDate completedDate;
    private String serviceType;
    private String triggeredBy;
    private String reason;
    private int taskCount;
    private BigDecimal totalCost;
    private String status;
    private String vendorName;
    private Integer nextServiceDueOdometer;
    private LocalDateTime raisedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String durationLabel;
}
