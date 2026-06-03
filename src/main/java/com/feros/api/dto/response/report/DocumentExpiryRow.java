package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentExpiryRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private long daysLeft;
    private String expiryStatus; // GREEN / AMBER / RED
}
