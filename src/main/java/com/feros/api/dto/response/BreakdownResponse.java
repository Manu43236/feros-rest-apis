package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BreakdownResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long vehicleAllocationId;
    private Long orderId;
    private String orderNumber;
    private LocalDateTime breakdownDate;
    private String location;
    private String breakdownType;
    private String reason;
    private String status;
    private Long replacementVehicleAllocationId;
    private String replacementVehicleRegistrationNumber;
    private LocalDateTime resolvedAt;
    private Long reportedById;
    private String reportedByName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
