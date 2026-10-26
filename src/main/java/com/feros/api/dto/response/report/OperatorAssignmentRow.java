package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of the equipment operator/cleaner assignment report (standing + shift). Equipment mirror of
 *  the driver-assignments report. */
@Getter
@Builder
public class OperatorAssignmentRow {
    private String staffName;
    private String role;             // Operator / Cleaner
    private String machine;          // registration / serial
    private String equipmentType;
    private String workOrderNumber;
    private String clientName;
    private String assignmentType;   // "Standing" or "Shift N · 07:00–15:00"
    private LocalDate fromDate;
    private LocalDate toDate;         // null = still active (standing)
    private BigDecimal pay;           // shift pay snapshot (null for standing — paid via normal wage)
}
