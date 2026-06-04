package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreRequestRow {
    private Long requestId;
    private LocalDateTime createdAt;
    private String vehicleRegistration;
    private String vehicleType;
    private String position;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String issuedTyreSerial;
    private String issuedTyreBrand;
    private double fittedAtKm;
    private String status; // PENDING / APPROVED / REJECTED
}
