package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EquipmentDocumentExpiryRow {
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private String equipmentType;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuerName;
    private BigDecimal cost;
    private LocalDate paidOn;
    private Long daysLeft;
    private String expiryStatus; // GREEN | AMBER | RED
}
