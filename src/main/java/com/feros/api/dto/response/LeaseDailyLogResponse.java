package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class LeaseDailyLogResponse {
    private Long id;
    private Long assignmentId;
    private Long leaseId;
    private LocalDate logDate;
    private String registrationNumber;
    private BigDecimal totalHours;
    private BigDecimal kmDriven;
    private Integer sessionCount;
    private String source; // AUTO or MANUAL
    private String notes;
    private LocalDateTime createdAt;
}
