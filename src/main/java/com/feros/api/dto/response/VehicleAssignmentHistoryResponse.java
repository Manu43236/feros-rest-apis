package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VehicleAssignmentHistoryResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private BigDecimal allocatedWeight;
    private String allocationStatus;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private String unassignedByName;
    private LocalDateTime unassignedAt;
}
