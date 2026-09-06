package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffAssignmentHistoryResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long userId;
    private String userName;
    private String userRole;
    private String action; // "Assigned" or "Unassigned"
    private String actionByName;
    private LocalDateTime actionAt;
    private String type; // "VEHICLE_ASSIGNMENT" | "ORDER_ALLOCATION" | "LEASE_ASSIGNMENT"
    private String orderNumber;
    private String leaseNumber;
}
