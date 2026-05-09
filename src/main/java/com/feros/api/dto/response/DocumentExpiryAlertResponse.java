package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DocumentExpiryAlertResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private long daysUntilExpiry;
    private boolean expired;
}
