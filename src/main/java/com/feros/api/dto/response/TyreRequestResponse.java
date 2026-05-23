package com.feros.api.dto.response;

import com.feros.api.enums.TyreRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TyreRequestResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long positionId;
    private String positionCode;
    private Long issuedTyreId;
    private String issuedTyreSerialNumber;
    private String issuedTyreBrand;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private TyreRequestStatus status;
    private String rejectionReason;
    private BigDecimal fittedAtKm;
    private String notes;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
