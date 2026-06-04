package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartRequestRow {
    private Long servicePartId;
    private LocalDate serviceDate;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private int quantityRequested;
    private int quantityApproved;
    private String vehicleRegistration;
    private String vehicleType;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String status; // REQUESTED / APPROVED / REJECTED
}
