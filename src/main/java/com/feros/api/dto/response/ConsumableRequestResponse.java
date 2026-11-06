package com.feros.api.dto.response;

import com.feros.api.enums.ConsumableRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumableRequestResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long sparePartId;
    private String sparePartName;
    private Integer quantityIssued;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private ConsumableRequestStatus status;
    private String rejectionReason;
    private String notes;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
