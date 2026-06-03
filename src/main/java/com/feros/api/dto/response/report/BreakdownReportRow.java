package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BreakdownReportRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private LocalDateTime breakdownDate;
    private String location;
    private String breakdownType;
    private String reason;
    private String status;
    private Long daysLost;
    private String reportedBy;
}
