package com.feros.api.dto.response;

import com.feros.api.enums.ServicePartStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePartResponse {
    private Long id;
    private Long serviceId;
    private String serviceNumber;
    private String vehicleRegistrationNumber;
    private Long sparePartId;
    private String partName;
    private String partNumber;
    private String unit;
    private Integer quantityRequested;
    private Integer quantityApproved;
    private ServicePartStatus status;
    private String rejectionReason;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
