package com.feros.api.dto.response;

import com.feros.api.enums.TireRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TireRequestResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long positionId;
    private String positionCode;
    private Long issuedTireId;
    private String issuedTireSerialNumber;
    private String issuedTireBrand;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private TireRequestStatus status;
    private String rejectionReason;
    private BigDecimal fittedAtKm;
    private String notes;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
